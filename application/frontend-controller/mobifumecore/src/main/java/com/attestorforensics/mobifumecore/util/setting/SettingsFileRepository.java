package com.attestorforensics.mobifumecore.util.setting;

import com.attestorforensics.mobifumecore.util.FileManager;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;

/**
 * Stores the global settings to file.
 */
class SettingsFileRepository implements SettingsRepository {

  private static final Gson GSON = new Gson();
  private final File oldSettingsFile;
  private final File settingsJsonFile;

  private SettingsFileRepository() {
    oldSettingsFile = new File(FileManager.getInstance().getDataFolder(), "settings");
    settingsJsonFile = new File(FileManager.getInstance().getDataFolder(), "settings.json");
  }

  static SettingsFileRepository create() {
    return new SettingsFileRepository();
  }

  @Override
  public Settings load() {
    if (oldSettingsFile.exists()) {
      migrateOldSettingsFile();
    }

    if (!settingsJsonFile.exists()) {
      return Settings.create();
    }

    try (FileReader fileReader = new FileReader(settingsJsonFile);
        JsonReader jsonReader = new JsonReader(fileReader)) {
      Settings settings = GSON.fromJson(jsonReader, Settings.class);
      return settings == null ? Settings.create() : settings;
    } catch (IOException e) {
      return Settings.create();
    }
  }

  @Override
  public void save(Settings settings) {
    try (Writer writer = new FileWriter(settingsJsonFile)) {
      GSON.toJson(settings, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Migrates old settings files from older application versions.
   */
  private void migrateOldSettingsFile() {
    try (FileInputStream fileInputStream = new FileInputStream(oldSettingsFile);
        ObjectInputStream stream = new ObjectInputStream(fileInputStream)) {
      Object object = stream.readObject();
      if (object instanceof Settings) {
        Settings settings = (Settings) object;
        save(settings);
      }
    } catch (IOException | ClassNotFoundException e) {
      // settings file is empty
    }

    oldSettingsFile.delete();
  }
}
