///////////////////////////////////////////////////////////////////////////////
//FILE:          MultipageTiffWriter.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Henry Pinkard, henry.pinkard@gmail.com, 2012
//
// COPYRIGHT:    University of California, San Francisco, 2012
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package org.micromanager.acquisition;


import ij.ImageJ;
import ij.io.TiffDecoder;
import ij.process.LUT;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import mmcorej.TaggedImage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.MMStudioMainFrame;
import org.micromanager.utils.ImageUtils;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

public class MultipageTiffWriter {
   
//   private static final long BYTES_PER_MEG = 1048576;
//   private static final long MAX_FILE_SIZE = 15*BYTES_PER_MEG;
   private static final long BYTES_PER_GIG = 1073741824;
   private static final long MAX_FILE_SIZE = 4 * BYTES_PER_GIG;
   public static final int DISPLAY_SETTINGS_BYTES_PER_CHANNEL = 256;
   //1 MB for now...might have to increase
   public static final long SPACE_FOR_COMMENTS = 1048576;
   public static final int INDEX_MAP_OFFSET_HEADER = 54773648;
   public static final int INDEX_MAP_HEADER = 3453623;
   public static final int DISPLAY_SETTINGS_OFFSET_HEADER = 483765892;
   public static final int DISPLAY_SETTINGS_HEADER = 347834724;
   public static final int COMMENTS_OFFSET_HEADER = 99384722;
   public static final int COMMENTS_HEADER = 84720485;
  
   public static final char ENTRIES_PER_IFD = 13;
   //Required tags
   public static final char WIDTH = 256;
   public static final char HEIGHT = 257;
   public static final char BITS_PER_SAMPLE = 258;
   public static final char COMPRESSION = 259;
   public static final char PHOTOMETRIC_INTERPRETATION = 262;
   public static final char IMAGE_DESCRIPTION = 270;
   public static final char STRIP_OFFSETS = 273;
   public static final char SAMPLES_PER_PIXEL = 277;
   public static final char ROWS_PER_STRIP = 278;
   public static final char STRIP_BYTE_COUNTS = 279;
   public static final char X_RESOLUTION = 282;
   public static final char Y_RESOLUTION = 283;
   public static final char RESOLUTION_UNIT = 296;
   public static final char IJ_METADATA_BYTE_COUNTS = TiffDecoder.META_DATA_BYTE_COUNTS;
   public static final char IJ_METADATA = TiffDecoder.META_DATA;
   public static final char MM_METADATA = 51123;
   
   public static final int SUMMARY_MD_HEADER = 2355492;
   
   private static ThreadPoolExecutor writingExecutor_ = null;
      
   public static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();
   
   final private boolean omeTiff_;
   
   private TaggedImageStorageMultipageTiff masterMPTiffStorage_;
   private RandomAccessFile raFile_;
   private FileChannel fileChannel_; 
   private long filePosition_ = 0;
   private int bufferPosition_;
   private int numChannels_ = 1, numFrames_ = 1, numSlices_ = 1;
   private HashMap<String, Long> indexMap_;
   private long nextIFDOffsetLocation_ = -1;
   private boolean rgb_ = false;
   private int byteDepth_, imageWidth_, imageHeight_, bytesPerImagePixels_;
   private long resNumerator_ = 1, resDenomenator_ = 1;
   private double zStepUm_ = 1;
   private LinkedList<ByteBuffer> buffers_;
   private boolean firstIFD_ = true;
   private long omeDescriptionTagPosition_;
   private long ijDescriptionTagPosition_;
   private long ijMetadataCountsTagPosition_;
   private long ijMetadataTagPosition_;
   //Reader associated with this file
   private MultipageTiffReader reader_;
   private long blankPixelsOffset_ = -1;
   private String summaryMDString_;
   private boolean fastStorageMode_;
   private int imageCount_ = 0;
   
   public MultipageTiffWriter(String directory, String filename, 
           JSONObject summaryMD, TaggedImageStorageMultipageTiff mpTiffStorage,
           boolean fastStorageMode) {
      fastStorageMode_ = fastStorageMode;
      masterMPTiffStorage_ = mpTiffStorage;
      omeTiff_ = mpTiffStorage.omeTiff_;        
      reader_ = new MultipageTiffReader(summaryMD);
      File f = new File(directory + "/" + filename); 
      
      try {
         processSummaryMD(summaryMD);
      } catch (MMScriptException ex1) {
         ReportingUtils.logError(ex1);
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
      }
      
      //This is an overestimate of file size because file gets truncated at end
      long fileSize = Math.min(MAX_FILE_SIZE, summaryMD.toString().length() + 2000000
              + numFrames_ * numChannels_ * numSlices_ * ((long) bytesPerImagePixels_ + 2000));
      
      try {
         f.createNewFile();
         raFile_ = new RandomAccessFile(f, "rw");
         try {
            raFile_.setLength(fileSize);
         } catch (IOException e) {       
          new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {}
                    MMStudioMainFrame.getInstance().getAcquisitionEngine().abortRequest();
                } }).start();     
                ReportingUtils.showError("Insufficent space on disk: no room to write data");
         }
         fileChannel_ = raFile_.getChannel();
         if (writingExecutor_ == null) {
            writingExecutor_ = fastStorageMode_
                    ? new ThreadPoolExecutor(1, 1, 0, TimeUnit.NANOSECONDS, new LinkedBlockingQueue())
                    : null;
         }
         indexMap_ = new HashMap<String, Long>();
         reader_.setFileChannel(fileChannel_);
         reader_.setIndexMap(indexMap_);
         buffers_ = new LinkedList<ByteBuffer>();
         
         writeMMHeaderAndSummaryMD(summaryMD);
      } catch (IOException ex) {
         ReportingUtils.logError(ex);
      }
      try {
         summaryMDString_ = summaryMD.toString(2);
      } catch (JSONException ex) {
         summaryMDString_ = "";
      }
   }
   
   private ByteBuffer allocateByteBuffer(int capacity) {
      return ByteBuffer.allocateDirect(capacity).order(BYTE_ORDER);
   }
   
   private BlockingQueue<ByteBuffer> currentImageByteBuffers_ = new LinkedBlockingQueue<ByteBuffer>(10);
   private int currentImageByteBufferCapacity_ = 0;
           
   private ByteBuffer allocateByteBufferMemo(int capacity) {
       if (capacity != currentImageByteBufferCapacity_) {
           currentImageByteBuffers_.clear();
           currentImageByteBufferCapacity_ = capacity;
       }
       
       ByteBuffer cachedBuf = currentImageByteBuffers_.poll();
       return (cachedBuf != null) ? cachedBuf : allocateByteBuffer(capacity);
   }
   
   private void executeWritingTask(Runnable writingTask) {
      if (fastStorageMode_) {
         writingExecutor_.execute(writingTask);
      } else {
         writingTask.run();
      }
   }
   
   private void fileChannelWrite(final ByteBuffer buffer, final long position) {
      executeWritingTask(
        new Runnable() {
           public void run() {
             try {
                buffer.rewind();
                fileChannel_.write(buffer, position);
                if (buffer.limit() == currentImageByteBufferCapacity_) {
                    currentImageByteBuffers_.offer(buffer);
                }
              } catch (IOException e) {
                ReportingUtils.logError(e);
              }
           }
        });
   }
   
   private void fileChannelWrite(final ByteBuffer[] buffers) {
      executeWritingTask(
        new Runnable() {
           public void run() {
             try {
                fileChannel_.write(buffers);
                for (ByteBuffer buffer:buffers) {
                    if (buffer.limit() == currentImageByteBufferCapacity_) {
                        currentImageByteBuffers_.offer(buffer);
                    }
                }
              } catch (IOException e) {
                ReportingUtils.logError(e);
              }
           }
        });
   }
   
   public MultipageTiffReader getReader() {
      return reader_;
   }
   
   public FileChannel getFileChannel() {
      return fileChannel_;
   }
   
   public HashMap<String, Long> getIndexMap() {
      return indexMap_;
   }
   
   private void writeMMHeaderAndSummaryMD(JSONObject summaryMD) throws IOException {      
      if (summaryMD.has("Comment")) {
         summaryMD.remove("Comment");
      }
      String summaryMDString = summaryMD.toString();
      int mdLength = summaryMDString.length();
      ByteBuffer buffer = allocateByteBuffer(40);
      if (BYTE_ORDER.equals(ByteOrder.BIG_ENDIAN)) {
         buffer.asCharBuffer().put(0,(char) 0x4d4d);
      } else {
         buffer.asCharBuffer().put(0,(char) 0x4949);
      }
      buffer.asCharBuffer().put(1,(char) 42);
      buffer.putInt(4,40 + mdLength);
      //8 bytes for file header +
      //8 bytes for index map offset header and offset +
      //8 bytes for display settings offset header and display settings offset
      //8 bytes for comments offset header and comments offset
      //8 bytes for summaryMD header  summary md length + 
      //1 byte for each character of summary md     
      buffer.putInt(32,SUMMARY_MD_HEADER);
      buffer.putInt(36,mdLength);
      ByteBuffer[] buffers = new ByteBuffer[2];
      buffers[0] = buffer;
      buffers[1] = ByteBuffer.wrap(getBytesFromString(summaryMDString));
      fileChannelWrite(buffers);
      filePosition_ += buffer.capacity() + mdLength;
   }
   
   /**
    * Called when there is no more data to be written. Write the index map, so if closing fails later 
    * on at least it will be there and have basic functionality in MM
    */
   public void finish() throws IOException {
      writeNullOffsetAfterLastImage();
      writeIndexMap();
   }

   /**
    * called when entire set of files (i.e. acquisition) is finished. Reopens file and writes
    * OME metadata, then closes it
    */
   public void close(String omeXML) throws IOException {
      String summaryComment = "";
      try 
      {
         JSONObject comments = masterMPTiffStorage_.getDisplayAndComments().getJSONObject("Comments");;
         if (comments.has("Summary") && !comments.isNull("Summary")) {
            summaryComment = comments.getString("Summary");
         }    
      } catch (Exception e) {
         ReportingUtils.logError("Could't get acquisition summary comment from displayAndComments");
      }
      writeImageJMetadata( numChannels_, summaryComment);

      if (omeTiff_) {
         try {
            writeImageDescription(omeXML, omeDescriptionTagPosition_);                 
         } catch (Exception ex) {
            ReportingUtils.showError("Error writing OME metadata");
         }
      }
      writeImageDescription(getIJDescriptionString(), ijDescriptionTagPosition_); 
      
      writeDisplaySettings();
      writeComments();

      executeWritingTask(new Runnable() {
         public void run() {
            try {
               //extra byte of space, just to make sure nothing gets cut off
               raFile_.setLength(filePosition_ + 8);
            } catch (IOException ex) {
               ReportingUtils.logError(ex);
            }
            reader_.finishedWriting();
            //Dont close file channel and random access file becase Tiff reader still using them
            fileChannel_ = null;
            raFile_ = null;
         }
      });
   }
   
   public boolean hasSpaceToWrite(TaggedImage img, int omeMDLength) {
      int mdLength = img.tags.toString().length();
      int indexMapSize = indexMap_.size()*20 + 8;
      int IFDSize = ENTRIES_PER_IFD*12 + 4 + 16;
      //5 MB extra padding
      int extraPadding = 5000000; 
      long size = mdLength+indexMapSize+IFDSize+bytesPerImagePixels_+SPACE_FOR_COMMENTS+
      numChannels_ * DISPLAY_SETTINGS_BYTES_PER_CHANNEL + extraPadding + filePosition_;
      if (omeTiff_) {
         size += omeMDLength;
      }
      
      if ( size >= MAX_FILE_SIZE) {
         return false;
      }
      return true;
   }
   
   public boolean isClosed() {
      return raFile_ == null;
   }
   
   public void writeBlankImage(String label) throws IOException {
      writeBlankIFD();
      writeBuffers();
   }
        
   public void writeImage(TaggedImage img) throws IOException {
      imageCount_++;
      if (writingExecutor_ != null) {
         int queueSize = writingExecutor_.getQueue().size();
         int attemptCount = 0;
         while (queueSize > 20) {
            if (attemptCount == 0) {
               ReportingUtils.logMessage("Warning: writing queue behind by " + queueSize + " images.");
            }
            ++attemptCount;
            try {
               Thread.sleep(5);
            } catch (InterruptedException ex) {
               ReportingUtils.logError(ex);
            }
         }
      }
      long offset = filePosition_;
      writeIFD(img);
      indexMap_.put(MDUtils.getLabel(img.tags), offset);
      writeBuffers();
   }
   
   private void writeBuffers() throws IOException {
      ByteBuffer[] buffs = new ByteBuffer[buffers_.size()];
      for (int i = 0; i < buffs.length; i++) {
         buffs[i] = buffers_.removeFirst();
      }
      fileChannelWrite(buffs);
   }

   private void writeIFD(TaggedImage img) throws IOException {
      char numEntries = (char) ((firstIFD_  ? ENTRIES_PER_IFD + 4 : ENTRIES_PER_IFD));
      if (img.tags.has("Summary")) {
         img.tags.remove("Summary");
      }
      String mdString = img.tags.toString() + " ";

      //2 bytes for number of directory entries, 12 bytes per directory entry, 4 byte offset of next IFD
     //6 bytes for bits per sample if RGB, 16 bytes for x and y resolution, 1 byte per character of MD string
     //number of bytes for pixels
     int totalBytes = 2 + numEntries*12 + 4 + (rgb_?6:0) + 16 + mdString.length() + bytesPerImagePixels_;
     int IFDandBitDepthBytes = 2+ numEntries*12 + 4 + (rgb_?6:0);
     
     ByteBuffer ifdBuffer = allocateByteBuffer(IFDandBitDepthBytes);
     CharBuffer charView = ifdBuffer.asCharBuffer();
         
     long tagDataOffset = filePosition_ + 2 + numEntries*12 + 4;
     nextIFDOffsetLocation_ = filePosition_ + 2 + numEntries*12;
     
     bufferPosition_ = 0;
      charView.put(bufferPosition_,numEntries);
      bufferPosition_ += 2;
      writeIFDEntry(ifdBuffer,charView, WIDTH,(char)4,1,imageWidth_);
      writeIFDEntry(ifdBuffer,charView,HEIGHT,(char)4,1,imageHeight_);
      writeIFDEntry(ifdBuffer,charView,BITS_PER_SAMPLE,(char)3,rgb_?3:1,  rgb_? tagDataOffset:byteDepth_*8);
      if (rgb_) {
         tagDataOffset += 6;
      }
      writeIFDEntry(ifdBuffer,charView,COMPRESSION,(char)3,1,1);
      writeIFDEntry(ifdBuffer,charView,PHOTOMETRIC_INTERPRETATION,(char)3,1,rgb_?2:1);
      
      if (firstIFD_ ) {
         omeDescriptionTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer, charView, IMAGE_DESCRIPTION, (char) 2, 0, 0);
         ijDescriptionTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer, charView, IMAGE_DESCRIPTION, (char) 2, 0, 0);
      }
           
      writeIFDEntry(ifdBuffer,charView,STRIP_OFFSETS,(char)4,1, tagDataOffset );
      tagDataOffset += bytesPerImagePixels_;
      writeIFDEntry(ifdBuffer,charView,SAMPLES_PER_PIXEL,(char)3,1,(rgb_?3:1));
      writeIFDEntry(ifdBuffer,charView,ROWS_PER_STRIP, (char) 3, 1, imageHeight_);
      writeIFDEntry(ifdBuffer,charView,STRIP_BYTE_COUNTS, (char) 4, 1, bytesPerImagePixels_ );
      writeIFDEntry(ifdBuffer,charView,X_RESOLUTION, (char)5, 1, tagDataOffset);
      tagDataOffset += 8;
      writeIFDEntry(ifdBuffer,charView,Y_RESOLUTION, (char)5, 1, tagDataOffset);
      tagDataOffset += 8;
      writeIFDEntry(ifdBuffer,charView,RESOLUTION_UNIT, (char) 3,1,3);
      if (firstIFD_) {         
         ijMetadataCountsTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer,charView,IJ_METADATA_BYTE_COUNTS,(char)4,0,0);
         ijMetadataTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer,charView,IJ_METADATA,(char)1,0,0);
      }
      writeIFDEntry(ifdBuffer,charView,MM_METADATA,(char)2,mdString.length(),tagDataOffset);
      tagDataOffset += mdString.length();
      //NextIFDOffset
      ifdBuffer.putInt(bufferPosition_, (int)tagDataOffset);
      bufferPosition_ += 4;
      
      if (rgb_) {
         charView.put(bufferPosition_/2,(char) (byteDepth_*8));
         charView.put(bufferPosition_/2+1,(char) (byteDepth_*8));
         charView.put(bufferPosition_/2+2,(char) (byteDepth_*8));
      }
      buffers_.add(ifdBuffer);
      buffers_.add(getPixelBuffer(img));
      buffers_.add(getResolutionValuesBuffer());   
      buffers_.add(ByteBuffer.wrap(getBytesFromString(mdString)));
      
      filePosition_ += totalBytes;
      firstIFD_ = false;
   }

   private void writeIFDEntry(ByteBuffer buffer, CharBuffer cBuffer, char tag, char type, long count, long value) throws IOException {
      cBuffer.put(bufferPosition_ / 2, tag);
      cBuffer.put(bufferPosition_ / 2 + 1, type);
      buffer.putInt(bufferPosition_ + 4, (int) count);
      if (type ==3 && count == 1) {  //Left justify in 4 byte value field
         cBuffer.put(bufferPosition_/2 + 4, (char) value);
         cBuffer.put(bufferPosition_/2 + 5,(char) 0);
      } else {
         buffer.putInt(bufferPosition_ + 8, (int) value);
      }      
      bufferPosition_ += 12;
   }

   private ByteBuffer getResolutionValuesBuffer() throws IOException {
      ByteBuffer buffer = allocateByteBuffer(16);
      buffer.putInt(0,(int)resNumerator_);
      buffer.putInt(4,(int)resDenomenator_);
      buffer.putInt(8,(int)resNumerator_);
      buffer.putInt(12,(int)resDenomenator_);
      return buffer;
   }

   public void setAbortedNumFrames(int n) {
      numFrames_ = n;
   }

   private ByteBuffer getPixelBuffer(TaggedImage img) throws IOException {
      if (rgb_) {
         if (byteDepth_ == 1) {
            byte[] originalPix = (byte[]) img.pix;
            byte[] pix = new byte[originalPix.length * 3 / 4];
            int count = 0;
            for (int i = 0; i < originalPix.length; i++) {
               if ((i + 1) % 4 != 0) {
                  pix[count] = originalPix[i];
                  count++;
               }
            }
            return ByteBuffer.wrap(pix);
         } else {
            short[] originalPix = (short[]) img.pix;
            short[] pix = new short[originalPix.length * 3 / 4];
            int count = 0;
            for (int i = 0; i < originalPix.length; i++) {
               if ((i + 1) % 4 != 0) {
                  pix[count] = originalPix[i];
                  count++;
               }
            }
            ByteBuffer buffer = allocateByteBufferMemo(pix.length * 2);
            buffer.rewind();
            buffer.asShortBuffer().put(pix);
            return buffer;
         }
      } else {
         if (byteDepth_ == 1) {
            return ByteBuffer.wrap((byte[]) img.pix);
         } else {
            short[] pix = (short[]) img.pix;
            ByteBuffer buffer = allocateByteBufferMemo(pix.length * 2);
            buffer.rewind();
            buffer.asShortBuffer().put(pix);
            return buffer;
         }
      }
   }

   private void processSummaryMD(JSONObject summaryMD) throws MMScriptException, JSONException {
      rgb_ = MDUtils.isRGB(summaryMD);
      numChannels_ = MDUtils.getNumChannels(summaryMD);
      numFrames_ = MDUtils.getNumFrames(summaryMD);
      numSlices_ = MDUtils.getNumSlices(summaryMD);
      imageWidth_ = MDUtils.getWidth(summaryMD);
      imageHeight_ = MDUtils.getHeight(summaryMD);
      String pixelType = MDUtils.getPixelType(summaryMD);
      if (pixelType.equals("GRAY8") || pixelType.equals("RGB32") || pixelType.equals("RGB24")) {
         byteDepth_ = 1;
      } else if (pixelType.equals("GRAY16") || pixelType.equals("RGB64")) {
         byteDepth_ = 2;
      } else if (pixelType.equals("GRAY32")) {
         byteDepth_ = 3;
      } else {
         byteDepth_ = 2;
      }
      bytesPerImagePixels_ = imageHeight_ * imageWidth_ * byteDepth_ * (rgb_ ? 3 : 1);
      //Tiff resolution tag values
      double cmPerPixel = 0.0001;
      if (summaryMD.has("PixelSizeUm")) {
         try {
            cmPerPixel = 0.0001 * summaryMD.getDouble("PixelSizeUm");
         } catch (JSONException ex) {
         }
      } else if (summaryMD.has("PixelSize_um")) {
         try {
            cmPerPixel = 0.0001 * summaryMD.getDouble("PixelSize_um");
         } catch (JSONException ex) {
         }
      }
      double log = Math.log10(cmPerPixel);
      if (log >= 0) {
         resDenomenator_ = (long) cmPerPixel;
         resNumerator_ = 1;
      } else {
         resNumerator_ = (long) (1 / cmPerPixel);
         resDenomenator_ = 1;
      }
      
       if (summaryMD.has("z-step_um") && !summaryMD.isNull("z-step_um")) {
            zStepUm_ = summaryMD.getDouble("z-step_um");
       }
   }

   /**
    * writes channel LUTs and display ranges for composite mode Could also be
    * expanded to write ROIs, file info, slice labels, and overlays
    */
   private void writeImageJMetadata(int numChannels, String summaryComment) throws IOException {
      String info = summaryMDString_;
      if (summaryComment != null && summaryComment.length() > 0) {
         info = "Acquisition comments: \n" + summaryComment + "\n\n\n" + summaryMDString_;
      }
      //size entry (4 bytes) + 4 bytes file info size + 4 bytes for channel display 
      //ranges length + 4 bytes per channel LUT
      int mdByteCountsBufferSize = 4 + 4 + 4 + 4 * numChannels;
      int bufferPosition = 0;

      ByteBuffer mdByteCountsBuffer = allocateByteBuffer(mdByteCountsBufferSize);

      //nTypes is number actually written among: fileInfo, slice labels, display ranges, channel LUTS,
      //slice labels, ROI, overlay, and # of extra metadata entries
      int nTypes = 3; //file info, display ranges, and channel LUTs
      int mdBufferSize = 4 + nTypes * 8;
      
      //Header size: 4 bytes for magic number + 8 bytes for label (int) and count (int) of each type
      mdByteCountsBuffer.putInt(bufferPosition, 4 + nTypes * 8);
      bufferPosition += 4;

      //2 bytes per a character of file info
      mdByteCountsBuffer.putInt(bufferPosition, 2*info.length() );
      bufferPosition += 4;
      mdBufferSize += info.length()*2;
      
      //display ranges written as array of doubles (min, max, min, max, etc)
      mdByteCountsBuffer.putInt(bufferPosition, numChannels * 2 * 8);
      bufferPosition += 4;
      mdBufferSize += numChannels * 2 * 8;

      for (int i = 0; i < numChannels; i++) {
         //768 bytes per LUT
         mdByteCountsBuffer.putInt(bufferPosition, 768);
         bufferPosition += 4;
         mdBufferSize += 768;
      }

      //Header (1) File info (1) display ranges (1) LUTS (1 per channel)
      int numMDEntries = 3 + numChannels;
      ByteBuffer ifdCountAndValueBuffer = allocateByteBuffer(8);
      ifdCountAndValueBuffer.putInt(0, numMDEntries);
      ifdCountAndValueBuffer.putInt(4, (int) filePosition_);
      fileChannelWrite(ifdCountAndValueBuffer, ijMetadataCountsTagPosition_ + 4);

      fileChannelWrite(mdByteCountsBuffer, filePosition_);
      filePosition_ += mdByteCountsBufferSize;


      //Write metadata types and counts
      ByteBuffer mdBuffer = allocateByteBuffer(mdBufferSize);
      bufferPosition = 0;

      //All the ints declared below are non public field in TiffDecoder
      final int ijMagicNumber = 0x494a494a;
      mdBuffer.putInt(bufferPosition, ijMagicNumber);
      bufferPosition += 4;

      //Write ints for each IJ metadata field and its count
      final int fileInfo = 0x696e666f;
      mdBuffer.putInt(bufferPosition, fileInfo);
      bufferPosition += 4;
      mdBuffer.putInt(bufferPosition, 1);
      bufferPosition += 4;
      
      final int displayRanges = 0x72616e67;
      mdBuffer.putInt(bufferPosition, displayRanges);
      bufferPosition += 4;
      mdBuffer.putInt(bufferPosition, 1);
      bufferPosition += 4;

      final int luts = 0x6c757473;
      mdBuffer.putInt(bufferPosition, luts);
      bufferPosition += 4;
      mdBuffer.putInt(bufferPosition, numChannels);
      bufferPosition += 4;


      //write actual metadata
      //FileInfo
      for (char c : info.toCharArray()) {
         mdBuffer.putChar(bufferPosition, c);
         bufferPosition += 2;
      }
      try {
         JSONArray channels = masterMPTiffStorage_.getDisplayAndComments().getJSONArray("Channels");
         JSONObject channelSetting;
         for (int i = 0; i < numChannels; i++) {
            channelSetting = channels.getJSONObject(i);
            //Display Ranges: For each channel, write min then max
            mdBuffer.putDouble(bufferPosition, channelSetting.getInt("Min"));
            bufferPosition += 8;
            mdBuffer.putDouble(bufferPosition, channelSetting.getInt("Max"));
            bufferPosition += 8;
         }

         //LUTs
         for (int i = 0; i < numChannels; i++) {
            channelSetting = channels.getJSONObject(i);
            LUT lut = ImageUtils.makeLUT(new Color(channelSetting.getInt("Color")), channelSetting.getDouble("Gamma"));
            for (byte b : lut.getBytes()) {
               mdBuffer.put(bufferPosition, b);
               bufferPosition++;
            }
         }
      } catch (JSONException ex) {
         ReportingUtils.logError("Problem with displayAndComments: Couldn't write ImageJ display settings as a result");
      }

      ifdCountAndValueBuffer = allocateByteBuffer(8);
      ifdCountAndValueBuffer.putInt(0, mdBufferSize);
      ifdCountAndValueBuffer.putInt(4, (int) filePosition_);
      fileChannelWrite(ifdCountAndValueBuffer, ijMetadataTagPosition_ + 4);


      fileChannelWrite(mdBuffer, filePosition_);
      filePosition_ += mdBufferSize;
   }

   private String getIJDescriptionString() {
      StringBuffer sb = new StringBuffer();
      sb.append("ImageJ=" + ImageJ.VERSION + "\n");
      if (numChannels_ > 1) {
         sb.append("channels=" + numChannels_ + "\n");
      }
      if (numSlices_ > 1) {
         sb.append("slices=" + numSlices_ + "\n");
      }
      if (numFrames_ > 1) {
         sb.append("frames=" + numFrames_ + "\n");
      }
      if (numFrames_ > 1 || numSlices_ > 1 || numChannels_ > 1) {
         sb.append("hyperstack=true\n");
      }
      if (numChannels_ > 1 && numSlices_ > 1 && masterMPTiffStorage_.slicesFirst()) {
         sb.append("order=zct\n");
      }
      //cm so calibration unit is consistent with units used in Tiff tags
      sb.append("unit=um\n");
      if (numSlices_ > 1) {
         sb.append("spacing=" + zStepUm_ + "\n");
      }
      //write single channel contrast settings or display mode if multi channel
      try {             
         JSONObject channel0setting = masterMPTiffStorage_.getDisplayAndComments().getJSONArray("Channels").getJSONObject(0);
         if (numChannels_ == 1) {
            double min = channel0setting.getInt("Min");
            double max = channel0setting.getInt("Max");
            sb.append("min=" + min + "\n");
            sb.append("max=" + max + "\n");
         } else {
            int displayMode = channel0setting.getInt("DisplayMode");
            //COMPOSITE=1, COLOR=2, GRAYSCALE=3
            if (displayMode == 1) {
               sb.append("mode=composite\n");
            } else if (displayMode == 2) {
               sb.append("mode=color\n");
            } else if (displayMode==3) {
               sb.append("mode=gray\n");
            }    
         }
      } catch (JSONException ex) {}
             
      sb.append((char) 0);
      return new String(sb);
   }

   private void writeImageDescription(String value, long imageDescriptionTagOffset) throws IOException {
      //write first image IFD
      ByteBuffer ifdCountAndValueBuffer = allocateByteBuffer(8);
      ifdCountAndValueBuffer.putInt(0, value.length());
      ifdCountAndValueBuffer.putInt(4, (int) filePosition_);
      fileChannelWrite(ifdCountAndValueBuffer, imageDescriptionTagOffset + 4);

      //write String
      ByteBuffer buffer = ByteBuffer.wrap(getBytesFromString(value));
      fileChannelWrite(buffer, filePosition_);
      filePosition_ += buffer.capacity();
   }

   private byte[] getBytesFromString(String s) {
      try {
         return s.getBytes("UTF-8");
      } catch (UnsupportedEncodingException ex) {
         ReportingUtils.logError("Error encoding String to bytes");
         return null;
      }
   }

   private void writeNullOffsetAfterLastImage() throws IOException {
      ByteBuffer buffer = allocateByteBuffer(4);
      buffer.putInt(0, 0);
      fileChannelWrite(buffer, nextIFDOffsetLocation_);
   }

   private void writeComments() throws IOException {
      //Write 4 byte header, 4 byte number of bytes
      JSONObject comments;
      try {
         comments = masterMPTiffStorage_.getDisplayAndComments().getJSONObject("Comments");
      } catch (JSONException ex) {
         comments = new JSONObject();
      }
      String commentsString = comments.toString();
      ByteBuffer header = allocateByteBuffer(8);
      header.putInt(0, COMMENTS_HEADER);
      header.putInt(4, commentsString.length());
      ByteBuffer buffer = ByteBuffer.wrap(getBytesFromString(commentsString));
      fileChannelWrite(header, filePosition_);
      fileChannelWrite(buffer, filePosition_ + 8);

      ByteBuffer offsetHeader = allocateByteBuffer(8);
      offsetHeader.putInt(0, COMMENTS_OFFSET_HEADER);
      offsetHeader.putInt(4, (int) filePosition_);
      fileChannelWrite(offsetHeader, 24);
      filePosition_ += 8 + commentsString.length();
   }

   private void writeIndexMap() throws IOException {
      //Write 4 byte header, 4 byte number of entries, and 20 bytes for each entry
      int numMappings = indexMap_.size();
      ByteBuffer buffer = allocateByteBuffer(8 + 20 * numMappings);
      buffer.putInt(0, INDEX_MAP_HEADER);
      buffer.putInt(4, numMappings);
      int position = 2;
      for (String label : indexMap_.keySet()) {
         String[] indecies = label.split("_");
         for (String index : indecies) {
            buffer.putInt(4 * position, Integer.parseInt(index));
            position++;
         }
         buffer.putInt(4 * position, indexMap_.get(label).intValue());
         position++;
      }
      fileChannelWrite(buffer, filePosition_);

      ByteBuffer header = allocateByteBuffer(8);
      header.putInt(0, INDEX_MAP_OFFSET_HEADER);
      header.putInt(4, (int) filePosition_);
      fileChannelWrite(header, 8);
      filePosition_ += buffer.capacity();
   }

   private void writeDisplaySettings() throws IOException {
      JSONArray displaySettings;
      try {
         displaySettings = masterMPTiffStorage_.getDisplayAndComments().getJSONArray("Channels");
      } catch (JSONException ex) {
         displaySettings = new JSONArray();
      }
      int numReservedBytes = numChannels_ * DISPLAY_SETTINGS_BYTES_PER_CHANNEL;
      ByteBuffer header = allocateByteBuffer(8);
      ByteBuffer buffer = ByteBuffer.wrap(getBytesFromString(displaySettings.toString()));
      header.putInt(0, DISPLAY_SETTINGS_HEADER);
      header.putInt(4, numReservedBytes);
      fileChannelWrite(header, filePosition_);
      fileChannelWrite(buffer, filePosition_ + 8);

      ByteBuffer offsetHeader = allocateByteBuffer(8);
      offsetHeader.putInt(0, DISPLAY_SETTINGS_OFFSET_HEADER);
      offsetHeader.putInt(4, (int) filePosition_);
      fileChannelWrite(offsetHeader, 16);
      filePosition_ += numReservedBytes + 8;
   }
  
   private void writeBlankIFD() throws IOException {
//      boolean blankPixelsAlreadyWritten = blankPixelsOffset_ != -1;
      boolean blankPixelsAlreadyWritten = false;

      char numEntries = (char) (((firstIFD_ && omeTiff_) ? ENTRIES_PER_IFD + 2 : ENTRIES_PER_IFD)
              + (firstIFD_ ? 2 : 0));
     
      String mdString = "NULL ";

      //2 bytes for number of directory entries, 12 bytes per directory entry, 4 byte offset of next IFD
     //6 bytes for bits per sample if RGB, 16 bytes for x and y resolution, 1 byte per character of MD string
     //number of bytes for pixels
     int totalBytes = 2 + numEntries*12 + 4 + (rgb_?6:0) + 16 + mdString.length() 
             + (blankPixelsAlreadyWritten ? 0 : bytesPerImagePixels_);
     int IFDandBitDepthBytes = 2+ numEntries*12 + 4 + (rgb_?6:0);
     
     ByteBuffer ifdBuffer = allocateByteBuffer(IFDandBitDepthBytes);
     CharBuffer charView = ifdBuffer.asCharBuffer();
         
     long tagDataOffset = filePosition_ + 2 + numEntries*12 + 4;
     nextIFDOffsetLocation_ = filePosition_ + 2 + numEntries*12;
     
     bufferPosition_ = 0;
      charView.put(bufferPosition_,numEntries);
      bufferPosition_ += 2;
      writeIFDEntry(ifdBuffer,charView, WIDTH,(char)4,1,imageWidth_);
      writeIFDEntry(ifdBuffer,charView,HEIGHT,(char)4,1,imageHeight_);
      writeIFDEntry(ifdBuffer,charView,BITS_PER_SAMPLE,(char)3,rgb_?3:1,  rgb_? tagDataOffset:byteDepth_*8);
      if (rgb_) {
         tagDataOffset += 6;
      }
      writeIFDEntry(ifdBuffer,charView,COMPRESSION,(char)3,1,1);
      writeIFDEntry(ifdBuffer,charView,PHOTOMETRIC_INTERPRETATION,(char)3,1,rgb_?2:1);
      
      if (firstIFD_ && omeTiff_) {
                  omeDescriptionTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer, charView, IMAGE_DESCRIPTION, (char) 2, 0, 0);
      }     
      if (firstIFD_) {
         ijDescriptionTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer, charView, IMAGE_DESCRIPTION, (char) 2, 0, 0);
      }
           
      if (!blankPixelsAlreadyWritten) { //Write blank pixels
         writeIFDEntry(ifdBuffer, charView, STRIP_OFFSETS, (char) 4, 1, tagDataOffset);
         blankPixelsOffset_ = tagDataOffset;
         tagDataOffset += bytesPerImagePixels_;
      } else {
         writeIFDEntry(ifdBuffer, charView, STRIP_OFFSETS, (char) 4, 1, blankPixelsOffset_);
      }
      
      writeIFDEntry(ifdBuffer,charView,SAMPLES_PER_PIXEL,(char)3,1,(rgb_?3:1));
      writeIFDEntry(ifdBuffer,charView,ROWS_PER_STRIP, (char) 3, 1, imageHeight_);
      writeIFDEntry(ifdBuffer,charView,STRIP_BYTE_COUNTS, (char) 4, 1, bytesPerImagePixels_ );
      writeIFDEntry(ifdBuffer,charView,X_RESOLUTION, (char)5, 1, tagDataOffset);
      tagDataOffset += 8;
      writeIFDEntry(ifdBuffer,charView,Y_RESOLUTION, (char)5, 1, tagDataOffset);
      tagDataOffset += 8;
      writeIFDEntry(ifdBuffer,charView,RESOLUTION_UNIT, (char) 3,1,3);
      if (firstIFD_) {         
         ijMetadataCountsTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer,charView,IJ_METADATA_BYTE_COUNTS,(char)4,0,0);
         ijMetadataTagPosition_ = filePosition_ + bufferPosition_;
         writeIFDEntry(ifdBuffer,charView,IJ_METADATA,(char)1,0,0);
      }
      writeIFDEntry(ifdBuffer,charView,MM_METADATA,(char)2,mdString.length(),tagDataOffset);
      tagDataOffset += mdString.length();
      //NextIFDOffset
      ifdBuffer.putInt(bufferPosition_, (int)tagDataOffset);
      bufferPosition_ += 4;
      
      if (rgb_) {
         charView.put(bufferPosition_/2,(char) (byteDepth_*8));
         charView.put(bufferPosition_/2+1,(char) (byteDepth_*8));
         charView.put(bufferPosition_/2+2,(char) (byteDepth_*8));
      }
      buffers_.add(ifdBuffer);
      if (!blankPixelsAlreadyWritten) {
         buffers_.add(ByteBuffer.wrap(new byte[bytesPerImagePixels_]));
      }
      buffers_.add(getResolutionValuesBuffer());   
      buffers_.add(ByteBuffer.wrap(getBytesFromString(mdString)));
      
      filePosition_ += totalBytes;
      firstIFD_ = false;
   }
}
