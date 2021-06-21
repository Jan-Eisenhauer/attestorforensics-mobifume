package com.attestorforensics.mobifumecore;

import com.attestorforensics.mobifumecore.model.MobiModelManager;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.WifiConnection;
import com.attestorforensics.mobifumecore.model.connection.WindowsWifiConnection;
import com.attestorforensics.mobifumecore.model.listener.EventDispatcher;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import com.attestorforensics.mobifumecore.util.FileManager;
import com.attestorforensics.mobifumecore.util.OtherAppChecker;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
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
  private final Properties config;

  /**
   * Gets the event manager.
   */
  private EventDispatcher eventDispatcher;
  /**
   * Gets the model manager which establish connections and holds devices, filters and groups.
   */
  private ModelManager modelManager;

  private final ScheduledExecutorService scheduledExecutorService;
  private final WifiConnection wifiConnection;

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

    config = new Properties();
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
      config.load(in);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (OtherAppChecker.isAppActive(FileManager.getInstance().getDataFolder())) {
      logger.error("App already active");
      System.exit(1);
    }

    eventDispatcher = EventDispatcher.create(scheduledExecutorService);

    Settings globalSettings = Settings.loadGlobalSettings();
    Locale language = globalSettings.getLanguage();
    LocaleManager.getInstance().load(language);

    wifiConnection = WindowsWifiConnection.create(scheduledExecutorService);
    wifiConnection.connect();

    modelManager = new MobiModelManager(globalSettings, wifiConnection);
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

  public Properties getConfig() {
    return config;
  }

  public EventDispatcher getEventDispatcher() {
    return eventDispatcher;
  }

  public ModelManager getModelManager() {
    return modelManager;
  }

  public WifiConnection getWifiConnection() {
    return wifiConnection;
  }
}
