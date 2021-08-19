package com.attestorforensics.mobifumecore.model.setting;

public class EvaporateSettings {

  private static final EvaporateSettings defaultEvaporateSettings = create(120, 30);

  private final int heaterTemperature;
  private final int evaporateTime;

  private EvaporateSettings(int heaterTemperature, int evaporateTime) {
    this.heaterTemperature = heaterTemperature;
    this.evaporateTime = evaporateTime;
  }

  public static EvaporateSettings create(int heaterTemperature, int evaporateTime) {
    return new EvaporateSettings(heaterTemperature, evaporateTime);
  }

  public static EvaporateSettings getDefault() {
    return defaultEvaporateSettings;
  }

  public int heaterTemperature() {
    return heaterTemperature;
  }

  public EvaporateSettings heaterTemperature(int heaterTemperature) {
    return create(heaterTemperature, evaporateTime);
  }

  public int evaporateTime() {
    return evaporateTime;
  }

  public EvaporateSettings evaporateTime(int evaporateTime) {
    return create(heaterTemperature, evaporateTime);
  }
}
