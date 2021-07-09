package com.attestorforensics.mobifumecore.model.log;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class CustomLogger {

  public static final File LOG_DIRECTORY = new File(
      System.getenv("userprofile") + File.separator + "documents" + File.separator + "MOBIfume"
          + File.separator + "logs");
  private static final String SPLIT = ";";

  private CustomLogger() {
  }

  public static Logger createLogger(Class<?> clazz) {
    Logger logger = Logger.getLogger(clazz);
    try {
      PatternLayout layout = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}] %-5p - %m%n");
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      String dateTime = formatter.format(new Date(System.currentTimeMillis()));
      FileAppender appender = new FileAppender(layout,
          LOG_DIRECTORY.getAbsolutePath() + File.separator + dateTime + ".log", false);
      logger.addAppender(appender);
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.setLevel(Level.ALL);

    return logger;
  }

  public static Logger createGroupLogger(Room group) {
    Logger logger = Logger.getLogger(group.getSettings().getCycleCount() + "-" + group.getName());

    try {
      PatternLayout layout = new PatternLayout("[%d{yyyy-MM-dd HH:mm:ss}];%p;%m%n");
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
      String dateTime = formatter.format(new Date(System.currentTimeMillis()));
      String cycle = String.format("%03d", group.getSettings().getCycleCount());
      FileAppender appender =
          new FileAppender(layout, LOG_DIRECTORY + File.separator + dateTime + "." + cycle + ".run",
              false);
      logger.addAppender(appender);
    } catch (IOException e) {
      e.printStackTrace();
    }

    logger.setLevel(Level.ALL);
    return logger;
  }

  public static void logGroupHeader(Room group) {
    group.getLogger()
        .trace(join("HEAD", version(), group.getSettings().getCycleCount(), group.getName()));
  }

  private static String join(Object... elements) {
    String[] stringElements = new String[elements.length];
    for (int i = 0; i < elements.length; i++) {
      stringElements[i] = elements[i].toString();
    }
    return String.join(SPLIT, stringElements);
  }

  public static String version() {
    return Mobifume.getInstance().getProjectProperties().getProperty("groupId") + "."
        + Mobifume.getInstance().getProjectProperties().getProperty("artifactId") + ":"
        + Mobifume.getInstance().getProjectProperties().getProperty("version");
  }

  public static void logGroupSettings(Room group) {
    Settings settings = group.getSettings();
    info(group, "SETTINGS", settings.getHumidifyMax(), settings.getHumidifyPuffer(),
        settings.getHeaterTemperature(), settings.getHeatTimer(), settings.getPurgeTimer());
  }

  public static void info(Room group, Object... elements) {
    String info = join(elements);
    group.getLogger().info(info);
    System.out.println(info);
  }

  public static void logGroupState(Room group) {
    info(group, "STATE", group.getStatus(), group.isHumidifying(), group.isHumidifyMaxReached());
  }

  public static void logGroupDevices(Room group) {
    List<String> nodeList = group.getDevices()
        .stream()
        .map(mapper -> mapper.getDeviceId() + "," + mapper.getType())
        .collect(Collectors.toList());
    nodeList.add(0, "DEVICES");
    info(group, nodeList.toArray());
  }

  public static void logGroupBase(Room group, Base base) {
    info(group, "BASE", base.getDeviceId(), base.getRssi(), base.getTemperature(), base.getHumidity(),
        base.getHeaterSetpoint(), base.getHeaterTemperature(), base.getLatch());
  }

  public static void logGroupHum(Room group, Humidifier hum) {
    info(group, "HUM", hum.getDeviceId(), hum.getRssi(), hum.isHumidify(), hum.getLed1(), hum.getLed2());
  }

  public static void logGroupRemove(Room group) {
    info(group, "DESTROY");
  }

  public static void info(Object... elements) {
    String info = join(elements);
    Mobifume.getInstance().getLogger().info(info);
    System.out.println(info);
  }
}
