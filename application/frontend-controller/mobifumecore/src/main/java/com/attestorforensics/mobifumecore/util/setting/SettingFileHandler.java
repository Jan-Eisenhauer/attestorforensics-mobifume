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
    try {
      ObjectInputStream stream = new ObjectInputStream(new FileInputStream(settingsFile));
      Object obj = stream.readObject();
      stream.close();
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
    try {
      ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(settingsFile));
      stream.writeObject(settings);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
