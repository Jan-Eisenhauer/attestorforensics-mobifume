package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

public class GroupSettings {

  private static final GroupSettings defaultGroupSettings =
      builder().humidifySettings(HumidifySettings.getDefault())
          .evaporateSettings(EvaporateSettings.getDefault())
          .evaporantSettings(EvaporantSettings.getDefault())
          .purgeSettings(PurgeSettings.getDefault())
          .build();

  private final HumidifySettings humidifySettings;
  private final EvaporateSettings evaporateSettings;
  private final EvaporantSettings evaporantSettings;
  private final PurgeSettings purgeSettings;

  private GroupSettings(SettingsBuilder settingsBuilder) {
    this.humidifySettings = settingsBuilder.humidifySettings;
    this.evaporateSettings = settingsBuilder.evaporateSettings;
    this.evaporantSettings = settingsBuilder.evaporantSettings;
    this.purgeSettings = settingsBuilder.purgeSettings;
  }

  public static SettingsBuilder builder() {
    return new SettingsBuilder();
  }

  public static GroupSettings getDefault() {
    return defaultGroupSettings;
  }

  public HumidifySettings humidifySettings() {
    return humidifySettings;
  }

  public GroupSettings humidifySettings(HumidifySettings humidifySettings) {
    return builder().humidifySettings(humidifySettings)
        .evaporateSettings(evaporateSettings)
        .evaporantSettings(evaporantSettings)
        .purgeSettings(purgeSettings)
        .build();
  }

  public EvaporateSettings evaporateSettings() {
    return evaporateSettings;
  }

  public GroupSettings evaporateSettings(EvaporateSettings evaporateSettings) {
    return builder().humidifySettings(humidifySettings)
        .evaporateSettings(evaporateSettings)
        .evaporantSettings(evaporantSettings)
        .purgeSettings(purgeSettings)
        .build();
  }

  public EvaporantSettings evaporantSettings() {
    return evaporantSettings;
  }

  public GroupSettings evaporantSettings(EvaporantSettings evaporantSettings) {
    return builder().humidifySettings(humidifySettings)
        .evaporateSettings(evaporateSettings)
        .evaporantSettings(evaporantSettings)
        .purgeSettings(purgeSettings)
        .build();
  }

  public PurgeSettings purgeSettings() {
    return purgeSettings;
  }

  public GroupSettings purgeSettings(PurgeSettings purgeSettings) {
    return builder().humidifySettings(humidifySettings)
        .evaporateSettings(evaporateSettings)
        .evaporantSettings(evaporantSettings)
        .purgeSettings(purgeSettings)
        .build();
  }

  public static class SettingsBuilder {

    private HumidifySettings humidifySettings;
    private EvaporateSettings evaporateSettings;
    private EvaporantSettings evaporantSettings;
    private PurgeSettings purgeSettings;

    private SettingsBuilder() {
    }

    public GroupSettings build() {
      checkNotNull(humidifySettings);
      checkNotNull(evaporateSettings);
      checkNotNull(evaporantSettings);
      checkNotNull(purgeSettings);
      return new GroupSettings(this);
    }

    public SettingsBuilder humidifySettings(HumidifySettings humidifySettings) {
      checkNotNull(humidifySettings);
      this.humidifySettings = humidifySettings;
      return this;
    }

    public SettingsBuilder evaporateSettings(EvaporateSettings evaporateSettings) {
      checkNotNull(evaporateSettings);
      this.evaporateSettings = evaporateSettings;
      return this;
    }

    public SettingsBuilder evaporantSettings(EvaporantSettings evaporantSettings) {
      checkNotNull(evaporantSettings);
      this.evaporantSettings = evaporantSettings;
      return this;
    }

    public SettingsBuilder purgeSettings(PurgeSettings purgeSettings) {
      checkNotNull(purgeSettings);
      this.purgeSettings = purgeSettings;
      return this;
    }
  }
}
