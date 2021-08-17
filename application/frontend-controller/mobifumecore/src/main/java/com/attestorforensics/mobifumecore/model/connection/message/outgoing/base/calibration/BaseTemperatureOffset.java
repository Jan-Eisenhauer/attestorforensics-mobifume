package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseTemperatureOffset implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float temperatureOffset;

  private BaseTemperatureOffset(String deviceId, float temperatureOffset) {
    this.deviceId = deviceId;
    this.temperatureOffset = temperatureOffset;
  }

  public static BaseTemperatureOffset create(String deviceId, float temperatureOffset) {
    return new BaseTemperatureOffset(deviceId, temperatureOffset);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "Z;" + temperatureOffset;
  }
}
