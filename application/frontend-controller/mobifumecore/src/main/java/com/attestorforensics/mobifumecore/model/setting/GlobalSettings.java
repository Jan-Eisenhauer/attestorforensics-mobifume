package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;

public class GlobalSettings {

  private static final GlobalSettings defaultGlobalSettings =
      builder().groupTemplateSettings(GroupSettings.getDefault())
          .locale(Locale.GERMANY)
          .cycleNumber(0)
          .build();

  private final GroupSettings groupTemplateSettings;
  private final Locale locale;
  private final int cycleNumber;

  private GlobalSettings(GlobalSettingsBuilder globalSettingsBuilder) {
    this.groupTemplateSettings = globalSettingsBuilder.groupTemplateSettings;
    this.locale = globalSettingsBuilder.locale;
    this.cycleNumber = globalSettingsBuilder.cycleNumber;
  }

  public static GlobalSettingsBuilder builder() {
    return new GlobalSettingsBuilder();
  }

  public static GlobalSettingsBuilder builder(GlobalSettings globalSettings) {
    return new GlobalSettingsBuilder(globalSettings);
  }

  public static GlobalSettings getDefault() {
    return defaultGlobalSettings;
  }

  public GroupSettings groupTemplateSettings() {
    return groupTemplateSettings;
  }

  public GlobalSettings groupTemplateSettings(GroupSettings groupTemplateSettings) {
    return builder(this).groupTemplateSettings(groupTemplateSettings).build();
  }

  public Locale locale() {
    return locale;
  }

  public GlobalSettings locale(Locale locale) {
    return builder(this).locale(locale).build();
  }

  public int cycleNumber() {
    return cycleNumber;
  }

  public GlobalSettings increaseCycleNumber() {
    return builder(this).increaseCycleNumber().build();
  }

  public static class GlobalSettingsBuilder {

    private GroupSettings groupTemplateSettings;
    private Locale locale;
    private Integer cycleNumber;

    private GlobalSettingsBuilder() {
    }

    private GlobalSettingsBuilder(GlobalSettings globalSettings) {
      groupTemplateSettings = globalSettings.groupTemplateSettings;
      locale = globalSettings.locale;
      cycleNumber = globalSettings.cycleNumber;
    }

    public GlobalSettings build() {
      checkNotNull(groupTemplateSettings);
      checkNotNull(locale);
      checkNotNull(cycleNumber);
      return new GlobalSettings(this);
    }

    public GlobalSettingsBuilder groupTemplateSettings(GroupSettings groupTemplateSettings) {
      checkNotNull(groupTemplateSettings);
      this.groupTemplateSettings = groupTemplateSettings;
      return this;
    }

    public GlobalSettingsBuilder locale(Locale locale) {
      checkNotNull(locale);
      this.locale = locale;
      return this;
    }

    public GlobalSettingsBuilder cycleNumber(int cycleNumber) {
      this.cycleNumber = cycleNumber;
      return this;
    }

    public GlobalSettingsBuilder increaseCycleNumber() {
      checkNotNull(cycleNumber);
      cycleNumber++;
      return this;
    }
  }
}
