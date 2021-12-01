package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

public class EvaporateSettings {

  private static final EvaporateSettings defaultEvaporateSettings =
      builder().heaterSetpoint(120).evaporateDuration(30).build();

  private final int heaterSetpoint;
  private final int evaporateDuration;

  private EvaporateSettings(EvaporateSettingsBuilder evaporateSettingsBuilder) {
    this.heaterSetpoint = evaporateSettingsBuilder.heaterSetpoint;
    this.evaporateDuration = evaporateSettingsBuilder.evaporateDuration;
  }

  public static EvaporateSettingsBuilder builder() {
    return new EvaporateSettingsBuilder();
  }

  public static EvaporateSettingsBuilder builder(EvaporateSettings evaporateSettings) {
    return new EvaporateSettingsBuilder(evaporateSettings);
  }

  public static EvaporateSettings getDefault() {
    return defaultEvaporateSettings;
  }

  public int heaterSetpoint() {
    return heaterSetpoint;
  }

  public EvaporateSettings heaterSetpoint(int heaterSetpoint) {
    return builder(this).heaterSetpoint(heaterSetpoint).build();
  }

  public int evaporateDuration() {
    return evaporateDuration;
  }

  public EvaporateSettings evaporateDuration(int evaporateDuration) {
    return builder(this).evaporateDuration(evaporateDuration).build();
  }

  public static class EvaporateSettingsBuilder {

    private Integer heaterSetpoint;
    private Integer evaporateDuration;

    private EvaporateSettingsBuilder() {
    }

    private EvaporateSettingsBuilder(EvaporateSettings evaporateSettings) {
      heaterSetpoint = evaporateSettings.heaterSetpoint;
      evaporateDuration = evaporateSettings.evaporateDuration;
    }

    public EvaporateSettings build() {
      checkNotNull(heaterSetpoint);
      checkNotNull(evaporateDuration);
      return new EvaporateSettings(this);
    }

    public EvaporateSettingsBuilder heaterSetpoint(int heaterSetpoint) {
      this.heaterSetpoint = heaterSetpoint;
      return this;
    }

    public EvaporateSettingsBuilder evaporateDuration(int evaporateDuration) {
      this.evaporateDuration = evaporateDuration;
      return this;
    }
  }
}
