package com.attestorforensics.mobifume.model.object;

public class Calibration {

  private final float gradient;
  private final float offset;

  private Calibration(float gradient, float offset) {
    this.gradient = gradient;
    this.offset = offset;
  }

  public static Calibration createDefault() {
    return new Calibration(1, 0);
  }

  public static Calibration create(float gradient, float offset) {
    return new Calibration(gradient, offset);
  }

  public float getGradient() {
    return gradient;
  }

  public float getOffset() {
    return offset;
  }
}
