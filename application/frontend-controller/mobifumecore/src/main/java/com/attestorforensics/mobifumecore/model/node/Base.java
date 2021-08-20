package com.attestorforensics.mobifumecore.model.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseDuration;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseRequestCalibrationData;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseReset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseSetpoint;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHeaterGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHeaterOffset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityGradient;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration.BaseHumidityOffset;
import com.attestorforensics.mobifumecore.model.node.misc.BaseLatch;
import com.attestorforensics.mobifumecore.model.node.misc.Calibration;
import com.attestorforensics.mobifumecore.model.node.misc.DoubleSensor;
import java.util.Optional;

public class Base extends Device {

  private DoubleSensor temperature;
  private DoubleSensor humidity;
  private double heaterSetpoint;
  private DoubleSensor heaterTemperature;
  private BaseLatch baseLatch;
  private Calibration humidityCalibration;
  private Calibration heaterCalibration;

  private Base(MessageSender messageSender, String deviceId, int version) {
    super(messageSender, deviceId, version);
  }

  public static Base create(MessageSender messageSender, String deviceId, int version) {
    return new Base(messageSender, deviceId, version);
  }

  public void sendReset() {
    messageSender.send(BaseReset.create(deviceId));
  }

  public void sendHeaterSetpoint(int heaterSetpoint) {
    if (this.heaterSetpoint == heaterSetpoint) {
      return;
    }

    forceSendHeaterSetpoint(heaterSetpoint);
  }

  public void forceSendHeaterSetpoint(int heaterSetpoint) {
    messageSender.send(BaseSetpoint.create(deviceId, heaterSetpoint));
  }

  public void sendLatchPurge() {
    if (baseLatch != BaseLatch.PURGING) {
      forceSendLatchPurge();
    }
  }

  public void sendLatchCirculate() {
    if (baseLatch != BaseLatch.CIRCULATING) {
      forceSendLatchCirculate();
    }
  }

  public void forceSendLatchPurge() {
    messageSender.send(
        com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseLatch.purge(deviceId));
  }

  public void forceSendLatchCirculate() {
    messageSender.send(
        com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.BaseLatch.circulate(deviceId));
  }

  public void sendTime(int time) {
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

  public void setHumidityCalibration(Calibration humidityCalibration) {
    this.humidityCalibration = humidityCalibration;
  }

  public void setHeaterCalibration(Calibration heaterCalibration) {
    this.heaterCalibration = heaterCalibration;
  }

  public void sendHumidityCalibration(Calibration calibration) {
    humidityCalibration = calibration;
    messageSender.send(BaseHumidityGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseHumidityOffset.create(deviceId, calibration.getOffset()));
    messageSender.send(BaseRequestCalibrationData.create(deviceId));
  }

  public void sendHeaterCalibration(Calibration calibration) {
    heaterCalibration = calibration;
    messageSender.send(BaseHeaterGradient.create(deviceId, calibration.getGradient()));
    messageSender.send(BaseHeaterOffset.create(deviceId, calibration.getOffset()));
    messageSender.send(BaseRequestCalibrationData.create(deviceId));
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

  public BaseLatch getLatch() {
    return baseLatch;
  }

  public void setLatch(BaseLatch baseLatch) {
    this.baseLatch = baseLatch;
  }
}
