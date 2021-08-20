package com.attestorforensics.mobifumecore.model.setting;

public class PurgeSettings {

  private static final PurgeSettings defaultPurgeSettings = create(60);

  private final int purgeDuration;

  private PurgeSettings(int purgeDuration) {
    this.purgeDuration = purgeDuration;
  }

  public static PurgeSettings create(int purgeDuration) {
    return new PurgeSettings(purgeDuration);
  }

  public static PurgeSettings getDefault() {
    return defaultPurgeSettings;
  }

  public int purgeDuration() {
    return purgeDuration;
  }

  public PurgeSettings purgeDuration(int purgeDuration) {
    return create(purgeDuration);
  }
}
