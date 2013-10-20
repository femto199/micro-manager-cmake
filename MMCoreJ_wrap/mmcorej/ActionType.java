/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package mmcorej;

public final class ActionType {
  public final static ActionType NoAction = new ActionType("NoAction");
  public final static ActionType BeforeGet = new ActionType("BeforeGet");
  public final static ActionType AfterSet = new ActionType("AfterSet");
  public final static ActionType IsSequenceable = new ActionType("IsSequenceable");
  public final static ActionType AfterLoadSequence = new ActionType("AfterLoadSequence");
  public final static ActionType StartSequence = new ActionType("StartSequence");
  public final static ActionType StopSequence = new ActionType("StopSequence");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static ActionType swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + ActionType.class + " with value " + swigValue);
  }

  private ActionType(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private ActionType(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private ActionType(String swigName, ActionType swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static ActionType[] swigValues = { NoAction, BeforeGet, AfterSet, IsSequenceable, AfterLoadSequence, StartSequence, StopSequence };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}

