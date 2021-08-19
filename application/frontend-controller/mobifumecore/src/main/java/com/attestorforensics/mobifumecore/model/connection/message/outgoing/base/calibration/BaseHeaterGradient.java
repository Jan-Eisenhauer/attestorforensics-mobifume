package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseHeaterGradient implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float heaterGradient;

  private BaseHeaterGradient(String deviceId, float heaterGradient) {
    this.deviceId = deviceId;
    this.heaterGradient = heaterGradient;
  }

  public static BaseHeaterGradient create(String deviceId, float heaterGradient) {
    return new BaseHeaterGradient(deviceId, heaterGradient);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "Y;" + heaterGradient;
  }
}
