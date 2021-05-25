package com.attestorforensics.mobifumecore.model.object;

public class HumidityRecord {

  private final long timestamp;
  private final double value;

  public HumidityRecord(long timestamp, double value) {
    this.timestamp = timestamp;
    this.value = value;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public double getValue() {
    return value;
  }
}
