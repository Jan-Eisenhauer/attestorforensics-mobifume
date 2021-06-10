package com.attestorforensics.mobifumecore.util.setting;

import com.attestorforensics.mobifumecore.util.FileManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SettingFileHandler {

  private File settingsFile;

  SettingFileHandler() {
    settingsFile = new File(FileManager.getInstance().getDataFolder(), "settings");
  }

  public Settings load() {
    createFile();
    try (FileInputStream fileInputStream = new FileInputStream(settingsFile);
        ObjectInputStream stream = new ObjectInputStream(fileInputStream)) {
      Object obj = stream.readObject();
      if (obj != null) {
        return (Settings) obj;
      }
    } catch (IOException | ClassNotFoundException e) {
      // settings file is empty
    }

    Settings settings = new Settings();
    save(settings);
    return settings;
  }

  private void createFile() {
    if (!settingsFile.exists()) {
      try {
        settingsFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  void save(Settings settings) {
    createFile();
    try (FileOutputStream fileOutputStream = new FileOutputStream(settingsFile);
        ObjectOutputStream stream = new ObjectOutputStream(fileOutputStream)) {
      stream.writeObject(settings);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
