package com.attestorforensics.mobifumecore.util;

import java.io.File;

public class FileManager {

  private static FileManager instance;

  private final File dataFolder;

  private FileManager() {
    dataFolder = new File(System.getenv("LOCALAPPDATA"), "MOBIfume");
  }

  public static FileManager getInstance() {
    if (instance == null) {
      instance = new FileManager();
    }
    return instance;
  }

  public File getDataFolder() {
    return dataFolder;
  }
}
