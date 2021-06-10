package com.attestorforensics.mobifumecore.util.log;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.update.Updater;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The log mover tries to move all log files to a connected usb device.
 */
public class LogMover {

  private static final long DELAY_IN_SECONDS = 10;

  private final ScheduledExecutorService scheduledExecutorService;
  private final Updater updater;
  private final File defaultUsbDirectory;

  private LogMover(ScheduledExecutorService scheduledExecutorService, Updater updater) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.updater = updater;
    defaultUsbDirectory =
        new File(Mobifume.getInstance().getSettings().getProperty("usb_directory"));
  }

  public static LogMover create(ScheduledExecutorService scheduledExecutorService,
      Updater updater) {
    return new LogMover(scheduledExecutorService, updater);
  }

  /**
   * Start trying to move log files async to any connected usb device.
   */
  public void startMovingToUsb() {
    scheduledExecutorService.scheduleWithFixedDelay(this::tryMovingToUsb, 1L, DELAY_IN_SECONDS,
        TimeUnit.SECONDS);
  }

  private void tryMovingToUsb() {
    if (!defaultUsbDirectory.exists() || !defaultUsbDirectory.isDirectory()) {
      return;
    }

    if (updater.isUpdateDeviceConnected()) {
      return;
    }

    File[] files = CustomLogger.LOG_DIRECTORY.listFiles();
    if (files == null || files.length == 0) {
      return;
    }

    File usbDestination = new File(defaultUsbDirectory, "MOBIfume" + File.separator + "logs");
    try {
      usbDestination.mkdirs();

      for (File file : files) {
        Files.move(file.toPath(), new File(usbDestination, file.getName()).toPath(),
            StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (FileSystemException ignored) {
      // occurs for the currently used log file
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
