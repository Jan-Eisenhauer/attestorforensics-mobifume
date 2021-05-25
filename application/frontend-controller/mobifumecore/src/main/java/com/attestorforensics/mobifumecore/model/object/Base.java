package com.attestorforensics.mobifumecore.model.object;

import com.attestorforensics.mobifumecore.model.connection.ClientConnection;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

public class Base extends Device {

  @Getter
  @Setter
  private double temperature = -128;
  @Setter
  private double humidity = -128;
  @Getter
  @Setter
  private double heaterSetpoint = -128;
  @Getter
  @Setter
  private double heaterTemperature = -128;
  @Getter
  @Setter
  private int latch;
  private Calibration humidityCalibration;
  private Calibration temperatureCalibration;

  public Base(ClientConnection clientConnection, final String id, final int version) {
    super(clientConnection, DeviceType.BASE, id, version);
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
    getEncoder().baseTime(this, time);
  }

  public double getHumidity() {
    return humidity;
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
}
