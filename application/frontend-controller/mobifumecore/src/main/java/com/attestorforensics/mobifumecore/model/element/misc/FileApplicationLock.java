package com.attestorforensics.mobifumecore.model.element.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileApplicationLock implements ApplicationLock {

  private final File lockFile;

  private FileApplicationLock(File dataFolder) {
    this.lockFile = new File(dataFolder, "MOBIfume.lock");
  }

  public static ApplicationLock create(File dataFolder) {
    return new FileApplicationLock(dataFolder);
  }

  @Override
  public boolean lockApplication() {
    RandomAccessFile randomAccessFile;
    try {
      randomAccessFile = new RandomAccessFile(lockFile, "rw");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return false;
    }

    FileChannel channel = randomAccessFile.getChannel();
    FileLock lock = null;
    try {
      lock = channel.tryLock();
    } catch (IOException e) {
      e.printStackTrace();
    }

    final FileLock finalLock = lock;

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (finalLock != null && finalLock.isValid()) {
        try {
          finalLock.release();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (channel.isOpen()) {
        try {
          channel.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      try {
        randomAccessFile.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }));

    return lock != null;
  }
}
