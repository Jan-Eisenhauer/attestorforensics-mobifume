package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import java.util.Optional;

public class BaseOffline implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "OFFLINE";

  private final String deviceId;

  private BaseOffline(String deviceId) {
    this.deviceId = deviceId;
  }

  public static BaseOffline createFromPayload(String topic, String[] arguments) {
    String deviceId = topic.substring(TOPIC_PREFIX.length());
    return new BaseOffline(deviceId);
  }

  public static BaseOffline create(String deviceId) {
    return new BaseOffline(deviceId);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public static class Factory implements IncomingMessageFactory<BaseOffline> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static BaseOffline.Factory create() {
      return new BaseOffline.Factory();
    }

    @Override
    public Optional<BaseOffline> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        return Optional.of(BaseOffline.createFromPayload(topic, arguments));
      }

      return Optional.empty();
    }
  }
}
