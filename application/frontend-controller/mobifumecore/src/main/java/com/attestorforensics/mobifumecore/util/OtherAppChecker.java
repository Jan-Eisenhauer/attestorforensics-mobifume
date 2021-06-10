package com.attestorforensics.mobifumecore.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Checks if another application is running on the same computer.
 */
public class OtherAppChecker {

  private OtherAppChecker() {
  }

  /**
   * Checks if another application of this type is running on the same computer. To check this, a
   * temporary file is created and locked. When the file is already locked then the application is
   * already running.
   *
   * @param dataFolder the data folder to store and lock the tmp file
   * @return if another app is active
   */
  public static boolean isAppActive(File dataFolder) {
    File file = new File(dataFolder, "MOBIfume.tmp");
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
      FileChannel channel = randomAccessFile.getChannel();
      FileLock lock = channel.tryLock();
      if (lock == null) {
        return true;
      }
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          lock.release();
          channel.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }));
    } catch (Exception e) {
      return false;
    }
    return false;
  }
}
