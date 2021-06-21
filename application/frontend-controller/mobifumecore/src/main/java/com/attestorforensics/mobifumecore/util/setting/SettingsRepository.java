package com.attestorforensics.mobifumecore.util.setting;

/**
 * Responsible for the store of the global settings.
 */
public interface SettingsRepository {

  /**
   * Loads the global settings.
   *
   * @return the stored global settings if exists otherwise the default settings
   */
  Settings load();

  /**
   * Saves the global settings.
   *
   * @param settings the settings to save
   */
  void save(Settings settings);
}
