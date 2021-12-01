package com.attestorforensics.mobifumecore.model.node.misc;

public class DoubleSensor {

  private static final double UNINITIALIZED_VALUE = -256;
  private static final double ERROR_VALUE = -128;

  private final DoubleSensorState state;
  private final double value;

  private DoubleSensor(double value) {
    this.value = value;
    state = DoubleSensorState.VALID;
  }

  private DoubleSensor(double value, DoubleSensorState state) {
    this.value = value;
    this.state = state;
  }

  public static DoubleSensor of(double value) {
    if (value == UNINITIALIZED_VALUE) {
      return uninitialized();
    } else if (value == ERROR_VALUE) {
      return error();
    } else {
      return new DoubleSensor(value);
    }
  }

  public static DoubleSensor uninitialized() {
    return new DoubleSensor(UNINITIALIZED_VALUE, DoubleSensorState.UNINITIALIZED);
  }

  public static DoubleSensor error() {
    return new DoubleSensor(ERROR_VALUE, DoubleSensorState.ERROR);
  }

  public double value() {
    return value;
  }

  public boolean isValid() {
    return state == DoubleSensorState.VALID;
  }

  public boolean isUninitialized() {
    return state == DoubleSensorState.UNINITIALIZED;
  }

  public boolean isError() {
    return state == DoubleSensorState.ERROR;
  }

  private enum DoubleSensorState {
    VALID,
    UNINITIALIZED,
    ERROR
  }
}
