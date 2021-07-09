package com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class HumidifierEnable implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/cmd/";

  private final String deviceId;
  private final boolean enable;

  private HumidifierEnable(String deviceId, boolean enable) {
    this.deviceId = deviceId;
    this.enable = enable;
  }

  public static HumidifierEnable create(String deviceId, boolean enable) {
    return new HumidifierEnable(deviceId, enable);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "H;" + (enable ? "1" : "0");
  }
}
