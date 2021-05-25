package com.attestorforensics.mobifumecore.model.update;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.event.UpdateAvailableEvent;
import com.attestorforensics.mobifumecore.model.event.UpdateNotAvailableEvent;
import com.attestorforensics.mobifumecore.model.event.UpdatingEvent;
import com.attestorforensics.mobifumecore.model.listener.EventManager;
import com.attestorforensics.mobifumecore.util.FileManager;
import com.attestorforensics.mobifumecore.util.log.CustomLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Updater {

  private static final long DELAY_IN_SECONDS = 10;
  private static final File UPDATE_FILE = new File("D:\\update.tgz");
  private static final String UPDATE_DISPLAY_NAME = "UPDATE (D:)";
  private static final String PROJECT_PROPERTIES = "project.properties";
  private static final String MOBIFUME_JAR = "MOBIfume.jar";
  private static final File TMP_UPDATE_FILE =
      new File(FileManager.getInstance().getDataFolder(), "update.jar");
  private static final String JAVAW = "\\jre-8u221\\bin\\javaw.exe";

  private final ScheduledExecutorService scheduledExecutorService;
  private final EventManager eventManager;

  private boolean updateAvailable = false;
  private String newVersion;

  private Updater(ScheduledExecutorService scheduledExecutorService, EventManager eventManager) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.eventManager = eventManager;
  }

  public static Updater create(ScheduledExecutorService scheduledExecutorService,
      EventManager eventManager) {
    return new Updater(scheduledExecutorService, eventManager);
  }

  public void startCheckingForUpdate() {
    scheduledExecutorService.scheduleWithFixedDelay(this::checkUpdate, 3L, DELAY_IN_SECONDS,
        TimeUnit.SECONDS);
  }

  public boolean isUpdateDeviceConnected() {
    String usbDisplayName =
        FileSystemView.getFileSystemView().getSystemDisplayName(UPDATE_FILE.getParentFile());
    return usbDisplayName.equals(UPDATE_DISPLAY_NAME);
  }

  public void installUpdate() {
    if (!updateAvailable) {
      return;
    }

    Mobifume.getInstance().getLogger().info("Installing update");
    eventManager.call(UpdatingEvent.create(UpdatingState.COPY_FROM_USB));
    try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
        new GzipCompressorInputStream(new FileInputStream(UPDATE_FILE)))) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (!entry.getName().equals(MOBIFUME_JAR)) {
          continue;
        }

        FileOutputStream updateOutputStream = new FileOutputStream(TMP_UPDATE_FILE);
        IOUtils.copy(tarInput, updateOutputStream);
      }
    } catch (IOException e) {
      e.printStackTrace();
      Mobifume.getInstance().getLogger().info("Failed installing update");
      eventManager.call(UpdatingEvent.create(UpdatingState.FAILED));
      return;
    }

    eventManager.call(UpdatingEvent.create(UpdatingState.START_UPDATER));
    if (!startExternalUpdater()) {
      return;
    }

    eventManager.call(UpdatingEvent.create(UpdatingState.SUCCESS));

    System.exit(0);
  }

  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  public Optional<String> getNewVersion() {
    return Optional.ofNullable(newVersion);
  }

  private void checkUpdate() {
    if (!isUpdateDeviceConnected()) {
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    String usbDisplayName =
        FileSystemView.getFileSystemView().getSystemDisplayName(UPDATE_FILE.getParentFile());
    if (!usbDisplayName.equals(UPDATE_DISPLAY_NAME)) {
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    if (!UPDATE_FILE.exists()) {
      return;
    }

    Properties projectProperties = Mobifume.getInstance().getProjectProperties();
    Properties updateProjectProperties;
    try {
      updateProjectProperties = readUpdateProjectProperties();
    } catch (IOException e) {
      e.printStackTrace();
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    if (updateProjectProperties == null) {
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    String updateVersion = updateProjectProperties.getProperty("version");
    if (updateVersion == null || projectProperties.getProperty("version").equals(updateVersion)) {
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    if (!projectProperties.getProperty("artifactId")
        .equals(updateProjectProperties.getProperty("artifactId"))
        || !projectProperties.getProperty("groupId")
        .equals(updateProjectProperties.getProperty("groupId"))) {
      if (updateAvailable) {
        updateAvailable = false;
        eventManager.call(UpdateNotAvailableEvent.create());
      }

      return;
    }

    newVersion = updateVersion;

    if (updateAvailable) {
      // already notified about update availability
      return;
    }

    updateAvailable = true;
    Mobifume.getInstance().getLogger().info("Update available: " + newVersion);
    eventManager.call(UpdateAvailableEvent.create(newVersion));
  }

  private Properties readUpdateProjectProperties() throws IOException {
    try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
        new GzipCompressorInputStream(new FileInputStream(UPDATE_FILE)))) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (!entry.getName().equals(PROJECT_PROPERTIES)) {
          continue;
        }

        Properties projectProperties = new Properties();
        try (InputStreamReader tarReader = new InputStreamReader(tarInput)) {
          projectProperties.load(tarReader);
        }

        return projectProperties;
      }
    }

    return null;
  }

  private boolean startExternalUpdater() {
    File currentJar;
    try {
      currentJar =
          new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      Mobifume.getInstance().getLogger().info("Failed installing update");
      eventManager.call(UpdatingEvent.create(UpdatingState.FAILED));
      return false;
    }

    try {
      Mobifume.getInstance().getLogger().info("Starting external updater");
      Runtime.getRuntime()
          .exec("cmd /S /c \"\"" + currentJar.getParent() + JAVAW + "\" -jar \"" + currentJar.getParent()
              + "\\MOBIfumeUpdate.jar\" \"" + TMP_UPDATE_FILE.getAbsolutePath() + "\" \""
              + currentJar.getAbsolutePath() + "\"\"");
    } catch (IOException e) {
      e.printStackTrace();
      Mobifume.getInstance().getLogger().info("Failed installing update");
      eventManager.call(UpdatingEvent.create(UpdatingState.FAILED));
      return false;
    }

    return true;
  }
}
