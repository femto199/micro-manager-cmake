/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public class PropertySetting {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected PropertySetting(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(PropertySetting obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        MMCoreJJNI.delete_PropertySetting(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public PropertySetting(String deviceLabel, String prop, String value, boolean readOnly) {
    this(MMCoreJJNI.new_PropertySetting__SWIG_0(deviceLabel, prop, value, readOnly), true);
  }

  public PropertySetting(String deviceLabel, String prop, String value) {
    this(MMCoreJJNI.new_PropertySetting__SWIG_1(deviceLabel, prop, value), true);
  }

  public PropertySetting() {
    this(MMCoreJJNI.new_PropertySetting__SWIG_2(), true);
  }

  public String getDeviceLabel() {
    return MMCoreJJNI.PropertySetting_getDeviceLabel(swigCPtr, this);
  }

  public String getPropertyName() {
    return MMCoreJJNI.PropertySetting_getPropertyName(swigCPtr, this);
  }

  public boolean getReadOnly() {
    return MMCoreJJNI.PropertySetting_getReadOnly(swigCPtr, this);
  }

  public String getPropertyValue() {
    return MMCoreJJNI.PropertySetting_getPropertyValue(swigCPtr, this);
  }

  public String getKey() {
    return MMCoreJJNI.PropertySetting_getKey(swigCPtr, this);
  }

  public static String generateKey(String device, String prop) {
    return MMCoreJJNI.PropertySetting_generateKey(device, prop);
  }

  public String Serialize() {
    return MMCoreJJNI.PropertySetting_Serialize(swigCPtr, this);
  }

  public void Restore(String data) {
    MMCoreJJNI.PropertySetting_Restore(swigCPtr, this, data);
  }

  public String getVerbose() {
    return MMCoreJJNI.PropertySetting_getVerbose(swigCPtr, this);
  }

  public boolean isEqualTo(PropertySetting ps) {
    return MMCoreJJNI.PropertySetting_isEqualTo(swigCPtr, this, PropertySetting.getCPtr(ps), ps);
  }

}