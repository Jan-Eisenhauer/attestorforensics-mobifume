package com.attestorforensics.mobifume.util.setting;

import com.attestorforensics.mobifume.model.object.Evaporant;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

public class Settings implements Serializable {

  private static final long serialVersionUID = -4384611401364322327L;
  public static Settings DEFAULT_SETTINGS;
  private static SettingFileHandler fileHandler = new SettingFileHandler();
  @Getter
  @Setter
  private double humidifyMax = 80;

  @Getter
  @Setter
  private double humidifyPuffer = 0.3;

  @Getter

  @Setter
  private int heaterTemperature = 120;

  @Getter
  @Setter
  private int heatTimer = 30;

  @Getter
  @Setter
  private int purgeTimer = 60;

  @Getter
  @Setter
  private Evaporant evaporant = Evaporant.CYANACRYLAT;

  @Getter
  @Setter
  private double roomWidth = 5;

  @Getter
  @Setter
  private double roomDepth = 5;

  @Getter
  @Setter
  private double roomHeight = 2.5;

  @Getter
  @Setter
  private double evaporantAmountPerCm = evaporant.getAmountPerCm();

  @Getter
  private int cycleCount = 0;

  public Settings() {
  }

  public Settings(Settings settings) {
    humidifyMax = settings.getHumidifyMax();
    humidifyPuffer = settings.getHumidifyPuffer();
    heaterTemperature = settings.getHeaterTemperature();
    heatTimer = settings.getHeatTimer();
    purgeTimer = settings.getPurgeTimer();
    evaporant = settings.getEvaporant();
    roomWidth = settings.getRoomWidth();
    roomDepth = settings.getRoomDepth();
    roomHeight = settings.getRoomHeight();
    evaporantAmountPerCm = settings.getEvaporantAmountPerCm();

    cycleCount = getNextCycleCount();
  }

  private int getNextCycleCount() {
    DEFAULT_SETTINGS.cycleCount++;
    saveDefaultSettings();
    return DEFAULT_SETTINGS.cycleCount;
  }

  private static void saveDefaultSettings() {
    fileHandler.save(DEFAULT_SETTINGS);
  }

  public static void loadDefaultSettings() {
    DEFAULT_SETTINGS = fileHandler.load();
  }
}
