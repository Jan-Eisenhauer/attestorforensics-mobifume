package com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import java.util.Optional;

public class HumidifierOffline implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/status/";
  private static final String FIRST_ARGUMENT = "OFFLINE";

  private final String deviceId;

  private HumidifierOffline(String deviceId) {
    this.deviceId = deviceId;
  }

  public static HumidifierOffline createFromPayload(String topic) {
    String deviceId = topic.substring(TOPIC_PREFIX.length());
    return new HumidifierOffline(deviceId);
  }

  public static HumidifierOffline create(String deviceId) {
    return new HumidifierOffline(deviceId);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public static class Factory implements IncomingMessageFactory<HumidifierOffline> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static HumidifierOffline.Factory create() {
      return new HumidifierOffline.Factory();
    }

    @Override
    public Optional<HumidifierOffline> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        return Optional.of(HumidifierOffline.createFromPayload(topic));
      }

      return Optional.empty();
    }
  }
}
