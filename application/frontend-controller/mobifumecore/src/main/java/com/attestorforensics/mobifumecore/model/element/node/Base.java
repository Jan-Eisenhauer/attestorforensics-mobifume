package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.ClientConnection;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import java.util.Optional;

public class Base extends Device {

  // Devices only support a maximum time of 114 minutes
  private static final int MAX_TIME = 114;

  private double temperature = -128;
  private double humidity = -128;
  private double heaterSetpoint = -128;
  private double heaterTemperature = -128;
  private int latch;
  private Calibration humidityCalibration;
  private Calibration temperatureCalibration;

  public Base(ClientConnection clientConnection, final String id, final int version) {
    super(clientConnection, DeviceType.BASE, id, version);
  }

  @Override
  public void reset() {
    getEncoder().baseReset(this);
  }

  public void updateHeaterSetpoint(int heaterTemperature) {
    if (heaterSetpoint == heaterTemperature) {
      return;
    }
    forceUpdateHeaterSetpoint(heaterTemperature);
  }

  public void forceUpdateHeaterSetpoint(int heaterTemperature) {
    getEncoder().baseSetHeater(this, heaterTemperature);
  }

  public void updateLatch(boolean open) {
    if (open && latch == 1 || !open && latch == 0) {
      return;
    }

    forceUpdateLatch(open);
  }

  public void forceUpdateLatch(boolean open) {
    getEncoder().baseLatch(this, open);
  }

  public void updateTime(int time) {
    getEncoder().baseTime(this, Math.min(time, MAX_TIME));
  }

  public Optional<Calibration> getHumidityCalibration() {
    return Optional.ofNullable(humidityCalibration);
  }

  public Optional<Calibration> getTemperatureCalibration() {
    return Optional.ofNullable(temperatureCalibration);
  }

  public void requestCalibrationData() {
    getEncoder().baseRequestCalibrationData(this);
  }

  public void setCalibration(float humidityGradient, float humidityOffset,
      float temperatureGradient, float temperatureOffset) {
    humidityCalibration = Calibration.create(humidityGradient, humidityOffset);
    temperatureCalibration = Calibration.create(temperatureGradient, temperatureOffset);
  }

  public void resetCalibration() {
    updateHumidityCalibration(Calibration.createDefault());
    updateTemperatureCalibration(Calibration.createDefault());
  }

  public void updateHumidityCalibration(Calibration calibration) {
    humidityCalibration = calibration;
    getEncoder().baseHumGradient(this, calibration.getGradient());
    getEncoder().baseHumOffset(this, calibration.getOffset());
  }

  public void updateTemperatureCalibration(Calibration calibration) {
    temperatureCalibration = calibration;
    getEncoder().baseTempGradient(this, calibration.getGradient());
    getEncoder().baseTempOffset(this, calibration.getOffset());
  }

  public double getTemperature() {
    return temperature;
  }

  public void setTemperature(double temperature) {
    this.temperature = temperature;
  }

  public double getHumidity() {
    return humidity;
  }

  public void setHumidity(double humidity) {
    this.humidity = humidity;
  }

  public double getHeaterSetpoint() {
    return heaterSetpoint;
  }

  public void setHeaterSetpoint(double heaterSetpoint) {
    this.heaterSetpoint = heaterSetpoint;
  }

  public double getHeaterTemperature() {
    return heaterTemperature;
  }

  public void setHeaterTemperature(double heaterTemperature) {
    this.heaterTemperature = heaterTemperature;
  }

  public int getLatch() {
    return latch;
  }

  public void setLatch(int latch) {
    this.latch = latch;
  }
}