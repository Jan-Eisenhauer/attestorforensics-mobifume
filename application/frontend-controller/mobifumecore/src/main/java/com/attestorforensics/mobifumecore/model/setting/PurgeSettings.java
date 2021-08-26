package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

public class PurgeSettings {

  private static final PurgeSettings defaultPurgeSettings = builder().purgeDuration(60).build();

  private final int purgeDuration;

  private PurgeSettings(PurgeSettingsBuilder purgeSettingsBuilder) {
    this.purgeDuration = purgeSettingsBuilder.purgeDuration;
  }

  public static PurgeSettingsBuilder builder() {
    return new PurgeSettingsBuilder();
  }

  public static PurgeSettingsBuilder builder(PurgeSettings purgeSettings) {
    return new PurgeSettingsBuilder(purgeSettings);
  }

  public static PurgeSettings getDefault() {
    return defaultPurgeSettings;
  }

  public int purgeDuration() {
    return purgeDuration;
  }

  public PurgeSettings purgeDuration(int purgeDuration) {
    return builder(this).purgeDuration(purgeDuration).build();
  }

  public static class PurgeSettingsBuilder {

    private Integer purgeDuration;

    private PurgeSettingsBuilder() {
    }

    private PurgeSettingsBuilder(PurgeSettings purgeSettings) {
      purgeDuration = purgeSettings.purgeDuration;
    }

    public PurgeSettings build() {
      checkNotNull(purgeDuration);
      return new PurgeSettings(this);
    }

    public PurgeSettingsBuilder purgeDuration(int purgeDuration) {
      this.purgeDuration = purgeDuration;
      return this;
    }
  }
}
