package com.attestorforensics.mobifumecore.model.setting;

public class HumidifySettings {

  private static final HumidifySettings defaultHumidifySettings = create(80, 0.3);

  private final int humiditySetpoint;
  private final double humidityPuffer;

  private HumidifySettings(int humiditySetpoint, double humidityPuffer) {
    this.humiditySetpoint = humiditySetpoint;
    this.humidityPuffer = humidityPuffer;
  }

  public static HumidifySettings create(int humiditySetpoint, double humidityPuffer) {
    return new HumidifySettings(humiditySetpoint, humidityPuffer);
  }

  public static HumidifySettings getDefault() {
    return defaultHumidifySettings;
  }

  public int humiditySetpoint() {
    return humiditySetpoint;
  }

  public HumidifySettings humiditySetpoint(int humiditySetpoint) {
    return create(humiditySetpoint, humidityPuffer);
  }

  public double humidityPuffer() {
    return humidityPuffer;
  }

  public HumidifySettings humidityPuffer(double humidityPuffer) {
    return create(humiditySetpoint, humidityPuffer);
  }
}
