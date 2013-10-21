/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public class MetadataSingleTag extends MetadataTag {
  private long swigCPtr;

  protected MetadataSingleTag(long cPtr, boolean cMemoryOwn) {
    super(MMCoreJJNI.MetadataSingleTag_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(MetadataSingleTag obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        MMCoreJJNI.delete_MetadataSingleTag(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public MetadataSingleTag() {
    this(MMCoreJJNI.new_MetadataSingleTag__SWIG_0(), true);
  }

  public MetadataSingleTag(String name, String device, boolean readOnly) {
    this(MMCoreJJNI.new_MetadataSingleTag__SWIG_1(name, device, readOnly), true);
  }

  public String GetValue() {
    return MMCoreJJNI.MetadataSingleTag_GetValue(swigCPtr, this);
  }

  public void SetValue(String val) {
    MMCoreJJNI.MetadataSingleTag_SetValue(swigCPtr, this, val);
  }

  public MetadataSingleTag ToSingleTag() {
    long cPtr = MMCoreJJNI.MetadataSingleTag_ToSingleTag(swigCPtr, this);
    return (cPtr == 0) ? null : new MetadataSingleTag(cPtr, false);
  }

  public MetadataTag Clone() {
    long cPtr = MMCoreJJNI.MetadataSingleTag_Clone(swigCPtr, this);
    return (cPtr == 0) ? null : new MetadataTag(cPtr, false);
  }

  public String Serialize() {
    return MMCoreJJNI.MetadataSingleTag_Serialize(swigCPtr, this);
  }

  public boolean Restore(String stream) {
    return MMCoreJJNI.MetadataSingleTag_Restore(swigCPtr, this, stream);
  }

}