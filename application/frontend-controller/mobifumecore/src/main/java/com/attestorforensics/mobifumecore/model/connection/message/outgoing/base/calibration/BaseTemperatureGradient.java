package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseTemperatureGradient implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float temperatureGradient;

  private BaseTemperatureGradient(String deviceId, float temperatureGradient) {
    this.deviceId = deviceId;
    this.temperatureGradient = temperatureGradient;
  }

  public static BaseTemperatureGradient create(String deviceId, float temperatureGradient) {
    return new BaseTemperatureGradient(deviceId, temperatureGradient);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "Y;" + temperatureGradient;
  }
}
