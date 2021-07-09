package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseDuration;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseLatch;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseRequestCalibrationData;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseReset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseSetpoint;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityOffset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseTemperatureGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseTemperatureOffset;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import com.attestorforensics.mobifumecore.model.element.misc.Latch;
import java.util.Optional;

public class Base extends Device {

  private double temperature = -128;
  private double humidity = -128;
  private double heaterSetpoint = -128;
  private double heaterTemperature = -128;
  private Latch latch;
  private Calibration humidityCalibration;
  private Calibration temperatureCalibration;

  public Base(MessageSender messageSender, final String deviceId, final int version) {
    super(messageSender, DeviceType.BASE, deviceId, version);
  }

  @Override
  public void reset() {
    messageSender.send(BaseReset.create(deviceId));
  }

  public void updateHeaterSetpoint(int heaterTemperature) {
    if (heaterSetpoint == heaterTemperature) {
      return;
    }

    forceUpdateHeaterSetpoint(heaterTemperature);
  }

  public void forceUpdateHeaterSetpoint(int heaterTemperature) {
    messageSender.send(BaseSetpoint.create(deviceId, heaterTemperature));
  }

  public void updateLatch(boolean open) {
    if (open && latch == Latch.CIRCULATING || !open && latch == Latch.PURGING) {
      return;
    }

    forceUpdateLatch(open);
  }

  public void forceUpdateLatch(boolean open) {
    messageSender.send(BaseLatch.create(deviceId, open));
  }

  public void updateTime(int time) {
    messageSender.send(BaseDuration.create(deviceId, time));
  }

  public Optional<Calibration> getHumidityCalibration() {
    return Optional.ofNullable(humidityCalibration);
  }

  public Optional<Calibration> getTemperatureCalibration() {
    return Optional.ofNullable(temperatureCalibration);
  }

  public void requestCalibrationData() {
    messageSender.send(BaseRequestCalibrationData.create(deviceId));
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
    messageSender.send(BaseHumidityGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseHumidityOffset.create(deviceId, calibration.getOffset()));
  }

  public void updateTemperatureCalibration(Calibration calibration) {
    temperatureCalibration = calibration;
    messageSender.send(BaseTemperatureGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseTemperatureOffset.create(deviceId, calibration.getOffset()));
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

  public Latch getLatch() {
    return latch;
  }

  public void setLatch(Latch latch) {
    this.latch = latch;
  }
}
