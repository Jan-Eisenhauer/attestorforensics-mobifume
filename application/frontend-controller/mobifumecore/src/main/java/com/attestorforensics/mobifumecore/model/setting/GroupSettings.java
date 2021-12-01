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

  private GroupSettings(GroupSettingsBuilder groupSettingsBuilder) {
    this.humidifySettings = groupSettingsBuilder.humidifySettings;
    this.evaporateSettings = groupSettingsBuilder.evaporateSettings;
    this.evaporantSettings = groupSettingsBuilder.evaporantSettings;
    this.purgeSettings = groupSettingsBuilder.purgeSettings;
  }

  public static GroupSettingsBuilder builder() {
    return new GroupSettingsBuilder();
  }

  public static GroupSettingsBuilder builder(GroupSettings groupSettings) {
    return new GroupSettingsBuilder(groupSettings);
  }

  public static GroupSettings getDefault() {
    return defaultGroupSettings;
  }

  public HumidifySettings humidifySettings() {
    return humidifySettings;
  }

  public GroupSettings humidifySettings(HumidifySettings humidifySettings) {
    return builder(this).humidifySettings(humidifySettings).build();
  }

  public EvaporateSettings evaporateSettings() {
    return evaporateSettings;
  }

  public GroupSettings evaporateSettings(EvaporateSettings evaporateSettings) {
    return builder(this).evaporateSettings(evaporateSettings).build();
  }

  public EvaporantSettings evaporantSettings() {
    return evaporantSettings;
  }

  public GroupSettings evaporantSettings(EvaporantSettings evaporantSettings) {
    return builder(this).evaporantSettings(evaporantSettings).build();
  }

  public PurgeSettings purgeSettings() {
    return purgeSettings;
  }

  public GroupSettings purgeSettings(PurgeSettings purgeSettings) {
    return builder(this).purgeSettings(purgeSettings).build();
  }

  public static class GroupSettingsBuilder {

    private HumidifySettings humidifySettings;
    private EvaporateSettings evaporateSettings;
    private EvaporantSettings evaporantSettings;
    private PurgeSettings purgeSettings;

    private GroupSettingsBuilder() {
    }

    private GroupSettingsBuilder(GroupSettings groupSettings) {
      humidifySettings = groupSettings.humidifySettings;
      evaporateSettings = groupSettings.evaporateSettings;
      evaporantSettings = groupSettings.evaporantSettings;
      purgeSettings = groupSettings.purgeSettings;
    }

    public GroupSettings build() {
      checkNotNull(humidifySettings);
      checkNotNull(evaporateSettings);
      checkNotNull(evaporantSettings);
      checkNotNull(purgeSettings);
      return new GroupSettings(this);
    }

    public GroupSettingsBuilder humidifySettings(HumidifySettings humidifySettings) {
      checkNotNull(humidifySettings);
      this.humidifySettings = humidifySettings;
      return this;
    }

    public GroupSettingsBuilder evaporateSettings(EvaporateSettings evaporateSettings) {
      checkNotNull(evaporateSettings);
      this.evaporateSettings = evaporateSettings;
      return this;
    }

    public GroupSettingsBuilder evaporantSettings(EvaporantSettings evaporantSettings) {
      checkNotNull(evaporantSettings);
      this.evaporantSettings = evaporantSettings;
      return this;
    }

    public GroupSettingsBuilder purgeSettings(PurgeSettings purgeSettings) {
      checkNotNull(purgeSettings);
      this.purgeSettings = purgeSettings;
      return this;
    }
  }
}
