/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public class PropertyBlock {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected PropertyBlock(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PropertyBlock obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        MMCoreJJNI.delete_PropertyBlock(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public PropertyBlock() {
    this(MMCoreJJNI.new_PropertyBlock(), true);
  }

  public void addPair(PropertyPair pair) {
    MMCoreJJNI.PropertyBlock_addPair(swigCPtr, this, PropertyPair.getCPtr(pair), pair);
  }

  public PropertyPair getPair(long index) throws java.lang.Exception {
    return new PropertyPair(MMCoreJJNI.PropertyBlock_getPair(swigCPtr, this, index), true);
  }

  public long size() {
    return MMCoreJJNI.PropertyBlock_size(swigCPtr, this);
  }

  public String getValue(String key) throws java.lang.Exception {
    return MMCoreJJNI.PropertyBlock_getValue(swigCPtr, this, key);
  }

}