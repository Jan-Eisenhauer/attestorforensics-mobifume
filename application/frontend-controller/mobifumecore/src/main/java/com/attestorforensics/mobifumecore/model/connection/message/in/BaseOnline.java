package com.attestorforensics.mobifumecore.model.connection.message.in;

import java.util.Optional;

public class BaseOnline implements IncomingMessage {

  //  private final Base base;
  private final String deviceId;

  private BaseOnline(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public static class Factory implements MessageFactory<BaseOnline> {

    private Factory() {
    }

    public static Factory create() {
      return new Factory();
    }

    @Override
    public Optional<BaseOnline> create(String topic, String[] arguments) {
      return Optional.empty();
    }
  }
}
