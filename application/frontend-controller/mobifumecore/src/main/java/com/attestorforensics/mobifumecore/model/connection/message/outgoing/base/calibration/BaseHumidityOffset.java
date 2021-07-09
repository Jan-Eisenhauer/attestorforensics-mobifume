package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseHumidityOffset implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float humidityOffset;

  private BaseHumidityOffset(String deviceId, float humidityOffset) {
    this.deviceId = deviceId;
    this.humidityOffset = humidityOffset;
  }

  public static BaseHumidityOffset create(String deviceId, float humidityOffset) {
    return new BaseHumidityOffset(deviceId, humidityOffset);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "H;" + humidityOffset;
  }
}
