package com.attestorforensics.mobifumemqtt.message;

public class HumPing {

  private final String deviceId;
  private final int rssi;
  private final HumidifyState humidify;
  private final LedState led1;
  private final LedState led2;
  private final boolean overTemperature;

  private HumPing(String deviceId, int rssi, HumidifyState humidify, LedState led1, LedState led2,
      boolean overTemperature) {
    this.deviceId = deviceId;
    this.rssi = rssi;
    this.humidify = humidify;
    this.led1 = led1;
    this.led2 = led2;
    this.overTemperature = overTemperature;
  }

  public static HumPing create(String deviceId, int rssi, HumidifyState humidify, LedState led1,
      LedState led2, boolean overTemperature) {
    return new HumPing(deviceId, rssi, humidify, led1, led2, overTemperature);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getRssi() {
    return rssi;
  }

  public HumidifyState getHumidify() {
    return humidify;
  }

  public LedState getLed1() {
    return led1;
  }

  public LedState getLed2() {
    return led2;
  }

  public boolean isOverTemperature() {
    return overTemperature;
  }

  public enum HumidifyState {
    ON("1"),
    OFF("0");

    private final String value;

    HumidifyState(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public enum LedState {
    ON("1"),
    OFF("0"),
    BLINKING("2");

    private final String value;

    LedState(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

}
