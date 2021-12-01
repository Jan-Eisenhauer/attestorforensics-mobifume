package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseLatch implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;
  private final boolean purge;

  private BaseLatch(String deviceId, boolean purge) {
    this.deviceId = deviceId;
    this.purge = purge;
  }

  public static BaseLatch purge(String deviceId) {
    return new BaseLatch(deviceId, true);
  }

  public static BaseLatch circulate(String deviceId) {
    return new BaseLatch(deviceId, false);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    return "L;" + (purge ? "1" : "0");
  }
}
