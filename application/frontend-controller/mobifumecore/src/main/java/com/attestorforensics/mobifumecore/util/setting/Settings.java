package com.attestorforensics.mobifumecore.util.setting;

import com.attestorforensics.mobifumecore.model.object.Evaporant;
import java.io.Serializable;
import java.util.Locale;

public class Settings implements Serializable {

  private static final long serialVersionUID = -4384611401364322327L;
  public static Settings DEFAULT_SETTINGS;
  private static final SettingFileHandler fileHandler = new SettingFileHandler();

  private Locale language = Locale.GERMANY;
  private int humidifyMax = 80;
  private double humidifyPuffer = 0.3;
  private int heaterTemperature = 120;
  private int heatTimer = 30;
  private int purgeTimer = 60;
  private Evaporant evaporant = Evaporant.CYANACRYLAT;
  private double roomWidth = 5;
  private double roomDepth = 5;
  private double roomHeight = 2.5;
  private double evaporantAmountPerCm = evaporant.getAmountPerCm();
  private int cycleCount = 0;

  public Settings() {
  }

  public Settings(Settings settings) {
    language = settings.getLanguage();
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

  public static void saveDefaultSettings() {
    fileHandler.save(DEFAULT_SETTINGS);
  }

  public static void loadDefaultSettings() {
    DEFAULT_SETTINGS = fileHandler.load();
  }

  public Locale getLanguage() {
    return language;
  }

  public void setLanguage(Locale language) {
    this.language = language;
  }

  public int getHumidifyMax() {
    return humidifyMax;
  }

  public void setHumidifyMax(int humidifyMax) {
    this.humidifyMax = humidifyMax;
  }

  public double getHumidifyPuffer() {
    return humidifyPuffer;
  }

  public int getHeaterTemperature() {
    return heaterTemperature;
  }

  public void setHeaterTemperature(int heaterTemperature) {
    this.heaterTemperature = heaterTemperature;
  }

  public int getHeatTimer() {
    return heatTimer;
  }

  public void setHeatTimer(int heatTimer) {
    this.heatTimer = heatTimer;
  }

  public int getPurgeTimer() {
    return purgeTimer;
  }

  public void setPurgeTimer(int purgeTimer) {
    this.purgeTimer = purgeTimer;
  }

  public Evaporant getEvaporant() {
    return evaporant;
  }

  public void setEvaporant(Evaporant evaporant) {
    this.evaporant = evaporant;
  }

  public double getRoomWidth() {
    return roomWidth;
  }

  public void setRoomWidth(double roomWidth) {
    this.roomWidth = roomWidth;
  }

  public double getRoomDepth() {
    return roomDepth;
  }

  public void setRoomDepth(double roomDepth) {
    this.roomDepth = roomDepth;
  }

  public double getRoomHeight() {
    return roomHeight;
  }

  public void setRoomHeight(double roomHeight) {
    this.roomHeight = roomHeight;
  }

  public double getEvaporantAmountPerCm() {
    return evaporantAmountPerCm;
  }

  public void setEvaporantAmountPerCm(double evaporantAmountPerCm) {
    this.evaporantAmountPerCm = evaporantAmountPerCm;
  }

  public int getCycleCount() {
    return cycleCount;
  }
}
