package com.attestorforensics.mobifume.util.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The log mover tries to move all log files to a connected usb device.
 */
public class LogMover {

  private static final long DELAY_IN_SECONDS = 10;
  private static final File DEFAULT_USB_DIRECTORY = new File("D:\\");

  private final ScheduledExecutorService scheduledExecutorService;

  private LogMover(ScheduledExecutorService scheduledExecutorService) {
    this.scheduledExecutorService = scheduledExecutorService;
  }

  public static LogMover create(ScheduledExecutorService scheduledExecutorService) {
    return new LogMover(scheduledExecutorService);
  }

  /**
   * Start trying to move log files async to any connected usb device.
   */
  public void startMovingToUsb() {
    scheduledExecutorService.scheduleWithFixedDelay(this::tryMovingToUsb, 1L, DELAY_IN_SECONDS,
        TimeUnit.SECONDS);
  }

  private void tryMovingToUsb() {
    if (!DEFAULT_USB_DIRECTORY.exists() || !DEFAULT_USB_DIRECTORY.isDirectory()) {
      return;
    }

    File[] files = CustomLogger.LOG_DIRECTORY.listFiles();
    if (files == null || files.length == 0) {
      return;
    }

    File usbDestination = new File(DEFAULT_USB_DIRECTORY, "MOBIfume" + File.separator + "logs");
    try {
      usbDestination.mkdirs();

      for (File file : files) {
        Files.move(file.toPath(), new File(usbDestination, file.getName()).toPath());
      }
    } catch (FileSystemException ignored) {
      // occurs for the currently used log file
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
