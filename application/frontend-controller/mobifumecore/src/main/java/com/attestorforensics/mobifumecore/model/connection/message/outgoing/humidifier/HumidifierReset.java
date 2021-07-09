package com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class HumidifierReset implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/cmd/";

  private final String deviceId;

  private HumidifierReset(String deviceId) {
    this.deviceId = deviceId;
  }

  public static HumidifierReset create(String deviceId) {
    return new HumidifierReset(deviceId);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "R;1";
  }
}
