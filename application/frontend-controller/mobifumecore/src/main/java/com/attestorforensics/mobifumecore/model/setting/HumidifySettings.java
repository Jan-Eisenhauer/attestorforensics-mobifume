package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

public class HumidifySettings {

  private static final HumidifySettings defaultHumidifySettings =
      builder().humiditySetpoint(80).humidityPuffer(0.3).build();

  private final int humiditySetpoint;
  private final double humidityPuffer;

  private HumidifySettings(HumidifySettingsBuilder humidifySettingsBuilder) {
    this.humiditySetpoint = humidifySettingsBuilder.humiditySetpoint;
    this.humidityPuffer = humidifySettingsBuilder.humidityPuffer;
  }

  public static HumidifySettingsBuilder builder() {
    return new HumidifySettingsBuilder();
  }

  public static HumidifySettingsBuilder builder(HumidifySettings humidifySettings) {
    return new HumidifySettingsBuilder(humidifySettings);
  }

  public static HumidifySettings getDefault() {
    return defaultHumidifySettings;
  }

  public int humiditySetpoint() {
    return humiditySetpoint;
  }

  public HumidifySettings humiditySetpoint(int humiditySetpoint) {
    return builder(this).humiditySetpoint(humiditySetpoint).build();
  }

  public double humidityPuffer() {
    return humidityPuffer;
  }

  public HumidifySettings humidityPuffer(double humidityPuffer) {
    return builder(this).humidityPuffer(humidityPuffer).build();
  }

  public static class HumidifySettingsBuilder {

    private Integer humiditySetpoint;
    private Double humidityPuffer;

    private HumidifySettingsBuilder() {
    }

    private HumidifySettingsBuilder(HumidifySettings humidifySettings) {
      humiditySetpoint = humidifySettings.humiditySetpoint;
      humidityPuffer = humidifySettings.humidityPuffer;
    }

    public HumidifySettings build() {
      checkNotNull(humiditySetpoint);
      checkNotNull(humidityPuffer);
      return new HumidifySettings(this);
    }

    public HumidifySettings.HumidifySettingsBuilder humiditySetpoint(int humiditySetpoint) {
      this.humiditySetpoint = humiditySetpoint;
      return this;
    }

    public HumidifySettings.HumidifySettingsBuilder humidityPuffer(double humidityPuffer) {
      this.humidityPuffer = humidityPuffer;
      return this;
    }
  }
}
