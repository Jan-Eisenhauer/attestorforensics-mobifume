package com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import java.util.Optional;

public class HumidifierOnline implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/status/";
  private static final String FIRST_ARGUMENT = "ONLINE";

  private final String deviceId;
  private final int version;

  private HumidifierOnline(String deviceId, int version) {
    this.deviceId = deviceId;
    this.version = version;
  }

  public static HumidifierOnline createFromPayload(String topic, String[] arguments)
      throws InvalidMessageArgumentException {
    String deviceId = topic.substring(TOPIC_PREFIX.length());

    int version;
    try {
      version = arguments.length >= 2 ? Integer.parseInt(arguments[1]) : 0;
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Invalid version");
    }

    return new HumidifierOnline(deviceId, version);
  }

  public static HumidifierOnline create(String deviceId, int version) {
    return new HumidifierOnline(deviceId, version);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getVersion() {
    return version;
  }

  public static class Factory implements IncomingMessageFactory<HumidifierOnline> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static HumidifierOnline.Factory create() {
      return new HumidifierOnline.Factory();
    }

    @Override
    public Optional<HumidifierOnline> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        try {
          return Optional.of(HumidifierOnline.createFromPayload(topic, arguments));
        } catch (InvalidMessageArgumentException e) {
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }
}
