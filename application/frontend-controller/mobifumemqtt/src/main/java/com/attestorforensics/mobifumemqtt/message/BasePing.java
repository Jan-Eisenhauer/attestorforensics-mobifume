package com.attestorforensics.mobifumemqtt.message;

public class BasePing {

  private final String deviceId;
  private final int rssi;
  private final double temperature;
  private final double humidity;
  private final int heaterSetpoint;
  private final double heaterTemperature;
  private final int latch;

  private BasePing(String deviceId, int rssi, double temperature, double humidity,
      int heaterSetpoint, double heaterTemperature, int latch) {
    this.deviceId = deviceId;
    this.rssi = rssi;
    this.temperature = temperature;
    this.humidity = humidity;
    this.heaterSetpoint = heaterSetpoint;
    this.heaterTemperature = heaterTemperature;
    this.latch = latch;
  }

  public static BasePing create(String deviceId, int rssi, double temperature, double humidity,
      int heaterSetpoint, double heaterTemperature, int latch) {
    return new BasePing(deviceId, rssi, temperature, humidity, heaterSetpoint, heaterTemperature,
        latch);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getRssi() {
    return rssi;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getHumidity() {
    return humidity;
  }

  public double getHeaterSetpoint() {
    return heaterSetpoint;
  }

  public double getHeaterTemperature() {
    return heaterTemperature;
  }

  public int getLatch() {
    return latch;
  }
}
