package com.attestorforensics.mobifumecore.model.setting;

public class EvaporateSettings {

  private static final EvaporateSettings defaultEvaporateSettings = create(120, 30);

  private final int heaterTemperature;
  private final int evaporateDuration;

  private EvaporateSettings(int heaterTemperature, int evaporateDuration) {
    this.heaterTemperature = heaterTemperature;
    this.evaporateDuration = evaporateDuration;
  }

  public static EvaporateSettings create(int heaterTemperature, int evaporateDuration) {
    return new EvaporateSettings(heaterTemperature, evaporateDuration);
  }

  public static EvaporateSettings getDefault() {
    return defaultEvaporateSettings;
  }

  public int heaterTemperature() {
    return heaterTemperature;
  }

  public EvaporateSettings heaterTemperature(int heaterTemperature) {
    return create(heaterTemperature, evaporateDuration);
  }

  public int evaporateDuration() {
    return evaporateDuration;
  }

  public EvaporateSettings evaporateDuration(int evaporateDuration) {
    return create(heaterTemperature, evaporateDuration);
  }
}
