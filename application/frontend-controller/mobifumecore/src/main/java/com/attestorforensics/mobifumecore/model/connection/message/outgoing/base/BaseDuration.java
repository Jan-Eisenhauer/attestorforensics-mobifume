package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseDuration implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  // Base device only support a maximum time of 114 minutes
  private static final int MAX_TIME = 114;

  private final String deviceId;
  private final int duration;

  private BaseDuration(String deviceId, int duration) {
    this.deviceId = deviceId;
    this.duration = duration;
  }

  public static BaseDuration create(String deviceId, int duration) {
    return new BaseDuration(deviceId, Math.min(duration, MAX_TIME));
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "T;" + duration;
  }
}
