/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public class MetadataKeyError extends MetadataError {
  private long swigCPtr;

  protected MetadataKeyError(long cPtr, boolean cMemoryOwn) {
    super(MMCoreJJNI.MetadataKeyError_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MetadataKeyError obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        MMCoreJJNI.delete_MetadataKeyError(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

   public String getMessage() {
      return getMsg();
   }

  public MetadataKeyError() {
    this(MMCoreJJNI.new_MetadataKeyError(), true);
  }

}