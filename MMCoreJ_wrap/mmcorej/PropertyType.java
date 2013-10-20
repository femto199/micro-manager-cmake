/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public final class PropertyType {
  public final static PropertyType Undef = new PropertyType("Undef");
  public final static PropertyType String = new PropertyType("String");
  public final static PropertyType Float = new PropertyType("Float");
  public final static PropertyType Integer = new PropertyType("Integer");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static PropertyType swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + PropertyType.class + " with value " + swigValue);
  }

  private PropertyType(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private PropertyType(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private PropertyType(String swigName, PropertyType swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static PropertyType[] swigValues = { Undef, String, Float, Integer };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

