package com.attestorforensics.mobifumecore.model.setting;

/**
 * Responsible for the store of the global settings.
 */
public interface SettingsRepository {

  /**
   * Loads the global settings.
   *
   * @return the stored global settings if exists otherwise the default settings
   */
  GlobalSettings load();

  /**
   * Saves the global settings.
   *
   * @param settings the settings to save
   */
  void save(GlobalSettings settings);
}
