package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base.calibration;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseHeaterOffset implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final float heaterOffset;

  private BaseHeaterOffset(String deviceId, float heaterOffset) {
    this.deviceId = deviceId;
    this.heaterOffset = heaterOffset;
  }

  public static BaseHeaterOffset create(String deviceId, float heaterOffset) {
    return new BaseHeaterOffset(deviceId, heaterOffset);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "Z;" + heaterOffset;
  }
}
