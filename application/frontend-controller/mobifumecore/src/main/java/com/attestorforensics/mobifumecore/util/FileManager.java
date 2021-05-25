package com.attestorforensics.mobifumecore.util;

import java.io.File;
import lombok.Getter;

public class FileManager {

  private static FileManager instance;

  @Getter
  private File dataFolder;

  private FileManager() {
    dataFolder = new File(System.getenv("LOCALAPPDATA"), "MOBIfume");
  }

  public static FileManager getInstance() {
    if (instance == null) {
      instance = new FileManager();
    }
    return instance;
  }
}
