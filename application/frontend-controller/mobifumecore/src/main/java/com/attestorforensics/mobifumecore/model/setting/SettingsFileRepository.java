package com.attestorforensics.mobifumecore.model.setting;

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
import java.nio.file.Files;

/**
 * Stores the global settings to file.
 */
public class SettingsFileRepository implements SettingsRepository {

  private static final Gson GSON = new Gson();
  private final File oldSettingsFile;
  private final File settingsJsonFile;

  private SettingsFileRepository() {
    oldSettingsFile = new File(FileManager.getInstance().getDataFolder(), "settings");
    settingsJsonFile = new File(FileManager.getInstance().getDataFolder(), "settings.json");
  }

  public static SettingsFileRepository create() {
    return new SettingsFileRepository();
  }

  @Override
  public GlobalSettings load() {
    if (oldSettingsFile.exists()) {
      migrateOldSettingsFile();
    }

    if (!settingsJsonFile.exists()) {
      return GlobalSettings.getDefault();
    }

    try (FileReader fileReader = new FileReader(settingsJsonFile);
        JsonReader jsonReader = new JsonReader(fileReader)) {

      GlobalSettings globalSettings = GSON.fromJson(jsonReader, GlobalSettings.class);

      // check if old settings are stored in settings.json
      if (globalSettings.locale() == null || globalSettings.groupTemplateSettings() == null) {
        globalSettings = readAndMigrateOldSettings();
      }

      return globalSettings;
    } catch (IOException e) {
      return GlobalSettings.getDefault();
    }
  }

  @Override
  public void save(GlobalSettings settings) {
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
        Settings oldSettings = (Settings) object;
        migrateOldSettings(oldSettings);
      }

      Files.delete(oldSettingsFile.toPath());
    } catch (IOException | ClassNotFoundException e) {
      // settings file is empty
    }
  }

  private GlobalSettings readAndMigrateOldSettings() throws IOException {
    try (FileReader fileReader = new FileReader(settingsJsonFile);
        JsonReader jsonReader = new JsonReader(fileReader)) {
      Settings oldSettings = GSON.fromJson(jsonReader, Settings.class);
      return migrateOldSettings(oldSettings);
    }
  }

  private GlobalSettings migrateOldSettings(Settings oldSettings) {
    HumidifySettings humidifySettings =
        HumidifySettings.create(oldSettings.getHumidifyMax(), oldSettings.getHumidifyPuffer());
    EvaporateSettings evaporateSettings =
        EvaporateSettings.create(oldSettings.getHeaterTemperature(), oldSettings.getHeatTimer());
    EvaporantSettings evaporantSettings = EvaporantSettings.builder()
        .evaporant(oldSettings.getEvaporant())
        .evaporantAmountPerCm(oldSettings.getEvaporantAmountPerCm())
        .roomWidth(oldSettings.getRoomWidth())
        .roomDepth(oldSettings.getRoomDepth())
        .roomHeight(oldSettings.getRoomHeight())
        .build();
    PurgeSettings purgeSettings = PurgeSettings.create(oldSettings.getPurgeTimer());
    GroupSettings groupTemplateSettings = GroupSettings.builder()
        .humidifySettings(humidifySettings)
        .evaporateSettings(evaporateSettings)
        .evaporantSettings(evaporantSettings)
        .purgeSettings(purgeSettings)
        .build();
    GlobalSettings globalSettings =
        GlobalSettings.create(groupTemplateSettings, oldSettings.getLanguage(),
            oldSettings.getCycleCount());

    save(globalSettings);
    return globalSettings;
  }
}
