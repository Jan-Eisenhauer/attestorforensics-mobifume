package com.attestorforensics.mobifumecore.model.setting;

public class PurgeSettings {

  private static final PurgeSettings defaultPurgeSettings = create(60);

  private final int purgeTime;

  private PurgeSettings(int purgeTime) {
    this.purgeTime = purgeTime;
  }

  public static PurgeSettings create(int purgeTime) {
    return new PurgeSettings(purgeTime);
  }

  public static PurgeSettings getDefault() {
    return defaultPurgeSettings;
  }

  public int purgeTime() {
    return purgeTime;
  }

  public PurgeSettings purgeTime(int purgeTime) {
    return create(purgeTime);
  }
}
