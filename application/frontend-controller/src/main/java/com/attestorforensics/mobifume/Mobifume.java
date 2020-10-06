package com.attestorforensics.mobifume;

import com.attestorforensics.mobifume.model.MobiModelManager;
import com.attestorforensics.mobifume.model.ModelManager;
import com.attestorforensics.mobifume.model.listener.EventManager;
import com.attestorforensics.mobifume.util.CustomLogger;
import com.attestorforensics.mobifume.util.FileManager;
import com.attestorforensics.mobifume.util.OtherAppChecker;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import com.attestorforensics.mobifume.view.MobiApplication;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import lombok.Getter;
import org.apache.log4j.Logger;

/**
 * Entry point of the program.
 */
public class Mobifume {

  /**
   * Gets the singleton instance of this class.
   */
  @Getter
  private static Mobifume instance;

  /**
   * Gets the custom logger for mobifume.
   */
  @Getter
  private final Logger logger = CustomLogger.createLogger(Mobifume.class);
  /**
   * Gets the project properties.
   */
  @Getter
  private final Properties projectProperties;
  /**
   * Gets the settings properties.
   */
  @Getter
  private final Properties settings;

  /**
   * Gets the event manager.
   */
  @Getter
  private EventManager eventManager;
  /**
   * Gets the model manager which establish connections and holds devices, filters and groups.
   */
  @Getter
  private ModelManager modelManager;

  private ScheduledExecutorService scheduledExecutorService;

  private Mobifume() {
    instance = this;

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(6);
    scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);
    scheduledExecutorService = scheduledThreadPoolExecutor;

    projectProperties = new Properties();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("project.properties")) {
      projectProperties.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }
    CustomLogger.info(CustomLogger.version());

    settings = new Properties();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("settings.properties")) {
      settings.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (OtherAppChecker.isAppActive(FileManager.getInstance().getDataFolder())) {
      logger.error("App already active");
      return;
    }

    eventManager = new EventManager();
    LocaleManager.getInstance().load();
    modelManager = new MobiModelManager();
    modelManager.connectWifi();
  }

  /**
   * Entry method point of the program.
   *
   * @param args the program parameters
   */
  public static void main(String[] args) {
    Mobifume mobifume = new Mobifume();
    mobifume.getScheduledExecutorService().execute(() -> MobiApplication.main(args));
  }

  public ScheduledExecutorService getScheduledExecutorService() {
    return scheduledExecutorService;
  }
}
