package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseLatch implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final boolean circulate;

  private BaseLatch(String deviceId, boolean circulate) {
    this.deviceId = deviceId;
    this.circulate = circulate;
  }

  public static BaseLatch create(String deviceId, boolean circulate) {
    return new BaseLatch(deviceId, circulate);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "L;" + (circulate ? "1" : "0");
  }
}
