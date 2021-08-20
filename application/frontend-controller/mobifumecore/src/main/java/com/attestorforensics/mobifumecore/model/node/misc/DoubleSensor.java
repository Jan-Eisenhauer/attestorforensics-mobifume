package com.attestorforensics.mobifumecore.model.node.misc;

public class DoubleSensor {

  private static final double ERROR_VALUE = -128;

  private final double value;
  private final boolean error;

  private DoubleSensor(double value) {
    this.value = value;
    this.error = false;
  }

  private DoubleSensor() {
    this.value = ERROR_VALUE;
    this.error = true;
  }

  public static DoubleSensor of(double value) {
    if (value == ERROR_VALUE) {
      return error();
    } else {
      return new DoubleSensor(value);
    }
  }

  public static DoubleSensor error() {
    return new DoubleSensor();
  }

  public double value() {
    return value;
  }

  public boolean isError() {
    return error;
  }

  public boolean isValid() {
    return !error;
  }
}
