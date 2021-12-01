package com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class HumidifierToggle implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/cmd/";

  private final String deviceId;
  private final boolean enable;

  private HumidifierToggle(String deviceId, boolean enable) {
    this.deviceId = deviceId;
    this.enable = enable;
  }

  public static HumidifierToggle enable(String deviceId) {
    return new HumidifierToggle(deviceId, true);
  }

  public static HumidifierToggle disable(String deviceId) {
    return new HumidifierToggle(deviceId, false);
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
