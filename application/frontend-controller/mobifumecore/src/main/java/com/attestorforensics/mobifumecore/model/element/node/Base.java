package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseDuration;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseLatch;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseRequestCalibrationData;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseReset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseSetpoint;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityOffset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHeaterGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHeaterOffset;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.misc.Latch;
import java.util.Optional;

public class Base extends Device {

  private DoubleSensor temperature;
  private DoubleSensor humidity;
  private double heaterSetpoint;
  private DoubleSensor heaterTemperature;
  private Latch latch;
  private Calibration humidityCalibration;
  private Calibration heaterCalibration;

  public Base(MessageSender messageSender, final String deviceId, final int version) {
    super(messageSender, deviceId, version);
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

  public Optional<Calibration> getHeaterCalibration() {
    return Optional.ofNullable(heaterCalibration);
  }

  public void requestCalibrationData() {
    messageSender.send(BaseRequestCalibrationData.create(deviceId));
  }

  public void setCalibration(float humidityGradient, float humidityOffset,
      float heaterGradient, float heaterOffset) {
    humidityCalibration = Calibration.create(humidityGradient, humidityOffset);
    heaterCalibration = Calibration.create(heaterGradient, heaterOffset);
  }

  public void resetCalibration() {
    updateHumidityCalibration(Calibration.createDefault());
    updateheaterCalibration(Calibration.createDefault());
  }

  public void updateHumidityCalibration(Calibration calibration) {
    humidityCalibration = calibration;
    messageSender.send(BaseHumidityGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseHumidityOffset.create(deviceId, calibration.getOffset()));
  }

  public void updateheaterCalibration(Calibration calibration) {
    heaterCalibration = calibration;
    messageSender.send(BaseHeaterGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseHeaterOffset.create(deviceId, calibration.getOffset()));
  }

  public DoubleSensor getTemperature() {
    return temperature;
  }

  public void setTemperature(DoubleSensor temperature) {
    this.temperature = temperature;
  }

  public DoubleSensor getHumidity() {
    return humidity;
  }

  public void setHumidity(DoubleSensor humidity) {
    this.humidity = humidity;
  }

  public double getHeaterSetpoint() {
    return heaterSetpoint;
  }

  public void setHeaterSetpoint(double heaterSetpoint) {
    this.heaterSetpoint = heaterSetpoint;
  }

  public DoubleSensor getHeaterTemperature() {
    return heaterTemperature;
  }

  public void setHeaterTemperature(DoubleSensor heaterTemperature) {
    this.heaterTemperature = heaterTemperature;
  }

  public Latch getLatch() {
    return latch;
  }

  public void setLatch(Latch latch) {
    this.latch = latch;
  }
}
