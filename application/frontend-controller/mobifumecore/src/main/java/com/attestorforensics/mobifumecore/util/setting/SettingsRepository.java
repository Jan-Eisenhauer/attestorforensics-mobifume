package com.attestorforensics.mobifumecore.util.setting;

public interface SettingsRepository {

  Settings load();

  void save(Settings settings);
}
