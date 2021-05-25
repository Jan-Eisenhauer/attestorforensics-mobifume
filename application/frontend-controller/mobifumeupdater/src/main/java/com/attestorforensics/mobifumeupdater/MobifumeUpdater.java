package com.attestorforensics.mobifumeupdater;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class MobifumeUpdater {

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length < 2) {
      System.out.println("Required arguments: <source> <destination>");
      return;
    }

    String source = args[0].replace("\"", "");
    String destination = args[1].replace("\"", "");
    File sourceFile = new File(source);
    File destinationFile = new File(destination);

    boolean success = false;
    while (!success) {
      try {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        success = true;
      } catch (FileSystemException ignored) {
        // try again if file system exception occurs (file used by other process)
        ignored.printStackTrace();
      }

      Thread.sleep(500L);
    }

    Runtime.getRuntime().exec("cmd /c shutdown -r -t 0");
  }
}
