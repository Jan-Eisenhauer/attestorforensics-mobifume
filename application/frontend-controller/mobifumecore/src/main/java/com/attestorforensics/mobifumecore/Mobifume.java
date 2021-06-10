package com.attestorforensics.mobifumecore;

import com.attestorforensics.mobifumecore.model.MobiModelManager;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.listener.EventManager;
import com.attestorforensics.mobifumecore.util.FileManager;
import com.attestorforensics.mobifumecore.util.OtherAppChecker;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import com.attestorforensics.mobifumecore.util.log.CustomLogger;
import com.attestorforensics.mobifumecore.util.setting.Settings;
import com.attestorforensics.mobifumecore.view.MobiApplication;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.apache.log4j.Logger;

/**
 * Entry point of the program.
 */
public class Mobifume {

  /**
   * Gets the singleton instance of this class.
   */
  private static Mobifume instance;

  /**
   * Gets the custom logger for mobifume.
   */
  private final Logger logger = CustomLogger.createLogger(Mobifume.class);
  /**
   * Gets the project properties.
   */
  private final Properties projectProperties;
  /**
   * Gets the settings properties.
   */
  private final Properties settings;

  /**
   * Gets the event manager.
   */
  private EventManager eventManager;
  /**
   * Gets the model manager which establish connections and holds devices, filters and groups.
   */
  private ModelManager modelManager;

  private final ScheduledExecutorService scheduledExecutorService;

  private Mobifume() {
    instance = this;

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
        new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
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

    Settings.loadDefaultSettings();

    if (OtherAppChecker.isAppActive(FileManager.getInstance().getDataFolder())) {
      logger.error("App already active");
      return;
    }

    eventManager = new EventManager();

    Locale language = Settings.DEFAULT_SETTINGS.getLanguage();
    if (language == null) {
      language = new Settings().getLanguage();
      Settings.DEFAULT_SETTINGS.setLanguage(language);
      Settings.saveDefaultSettings();
    }

    LocaleManager.getInstance().load(language);

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

  public static Mobifume getInstance() {
    return instance;
  }

  public ScheduledExecutorService getScheduledExecutorService() {
    return scheduledExecutorService;
  }

  public Logger getLogger() {
    return logger;
  }

  public Properties getProjectProperties() {
    return projectProperties;
  }

  public Properties getSettings() {
    return settings;
  }

  public EventManager getEventManager() {
    return eventManager;
  }

  public ModelManager getModelManager() {
    return modelManager;
  }
}
