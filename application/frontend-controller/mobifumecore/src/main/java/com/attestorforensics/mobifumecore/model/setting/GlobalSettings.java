package com.attestorforensics.mobifumecore.model.setting;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;

public class GlobalSettings {

  private static final GlobalSettings defaultGlobalSettings =
      create(GroupSettings.getDefault(), Locale.GERMANY, 0);

  private final GroupSettings groupTemplateSettings;
  // Older versions of settings.json stored locale as 'language'
  @SerializedName(value = "locale", alternate = "language")
  private final Locale locale;
  // Older versions of settings.json stored cycleNumber as 'cycleCount'
  @SerializedName(value = "cycleNumber", alternate = "cycleCount")
  private final int cycleNumber;

  private GlobalSettings(GroupSettings groupTemplateSettings, Locale locale, int cycleNumber) {
    this.groupTemplateSettings = groupTemplateSettings;
    this.locale = locale;
    this.cycleNumber = cycleNumber;
  }

  public static GlobalSettings create(GroupSettings groupTemplateSettings, Locale locale,
      int cycleNumber) {
    return new GlobalSettings(groupTemplateSettings, locale, cycleNumber);
  }

  public static GlobalSettings getDefault() {
    return defaultGlobalSettings;
  }

  public GroupSettings groupTemplateSettings() {
    return groupTemplateSettings;
  }

  public GlobalSettings groupTemplateSettings(GroupSettings groupTemplateSettings) {
    return create(groupTemplateSettings, locale, cycleNumber);
  }

  public Locale locale() {
    return locale;
  }

  public GlobalSettings locale(Locale locale) {
    return create(groupTemplateSettings, locale, cycleNumber);
  }

  public int cycleNumber() {
    return cycleNumber;
  }

  public GlobalSettings increaseCycleNumber() {
    return create(groupTemplateSettings, locale, cycleNumber + 1);
  }
}
