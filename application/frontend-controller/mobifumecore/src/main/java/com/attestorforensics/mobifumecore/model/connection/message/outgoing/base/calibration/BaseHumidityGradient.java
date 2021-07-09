package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseHumidityGradient implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float humidityGradient;

  private BaseHumidityGradient(String deviceId, float humidityGradient) {
    this.deviceId = deviceId;
    this.humidityGradient = humidityGradient;
  }

  public static BaseHumidityGradient create(String deviceId, float humidityGradient) {
    return new BaseHumidityGradient(deviceId, humidityGradient);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "I;" + humidityGradient;
  }
}
