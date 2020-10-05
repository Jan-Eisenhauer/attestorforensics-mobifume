package com.attestorforensics.mobifume.model.object;

import com.attestorforensics.mobifume.model.connection.ClientConnection;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

public class Base extends Device {

  private final BaseFileHandler baseFileHandler;

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
  private Float humidityOffset;
  private Float humidityGradient;
  private Float temperatureOffset;
  private Float temperatureGradient;

  public Base(ClientConnection clientConnection, final String id, final int version,
      BaseFileHandler baseFileHandler) {
    super(clientConnection, DeviceType.BASE, id, version);
    this.baseFileHandler = baseFileHandler;
    BaseFileHandler.BaseContent baseContent = baseFileHandler.loadBase(id);
    if (Objects.nonNull(baseContent)) {
      humidityOffset = baseContent.getHumidityOffset();
      humidityGradient = baseContent.getHumidityGradient();
      temperatureOffset = baseContent.getTemperatureOffset();
      temperatureGradient = baseContent.getTemperatureGradient();
    }
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

  public Optional<Float> getHumidityOffset() {
    return Optional.ofNullable(humidityOffset);
  }

  public void setHumidityOffset(float offset) {
    this.humidityOffset = offset;
    baseFileHandler.saveBase(this);
    clientConnection.getEncoder().baseHumOffset(this, humidityOffset);
  }

  public Optional<Float> getHumidityGradient() {
    return Optional.ofNullable(humidityGradient);
  }

  public void setHumidityGradient(float gradient) {
    this.humidityGradient = gradient;
    baseFileHandler.saveBase(this);
    clientConnection.getEncoder().baseHumGradient(this, humidityGradient);
  }

  public Optional<Float> getTemperatureOffset() {
    return Optional.ofNullable(temperatureOffset);
  }

  public void setTemperatureOffset(float offset) {
    this.temperatureOffset = offset;
    baseFileHandler.saveBase(this);
    clientConnection.getEncoder().baseTempOffset(this, temperatureOffset);
  }

  public Optional<Float> getTemperatureGradient() {
    return Optional.ofNullable(temperatureGradient);
  }

  public void setTemperatureGradient(float gradient) {
    this.temperatureGradient = gradient;
    baseFileHandler.saveBase(this);
    clientConnection.getEncoder().baseTempGradient(this, temperatureGradient);
  }
}
