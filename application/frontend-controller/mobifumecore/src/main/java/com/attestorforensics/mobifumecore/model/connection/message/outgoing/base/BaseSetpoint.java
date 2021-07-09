package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseSetpoint implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final double heaterSetpoint;

  private BaseSetpoint(String deviceId, double heaterSetpoint) {
    this.deviceId = deviceId;
    this.heaterSetpoint = heaterSetpoint;
  }

  public static BaseSetpoint create(String deviceId, double heaterSetpoint) {
    return new BaseSetpoint(deviceId, heaterSetpoint);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "F;" + String.format("%03d", heaterSetpoint);
  }
}
