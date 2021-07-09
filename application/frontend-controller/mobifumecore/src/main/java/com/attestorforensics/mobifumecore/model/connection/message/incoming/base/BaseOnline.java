package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import java.util.Optional;

public class BaseOnline implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "ONLINE";

  private final String deviceId;
  private final int version;

  private BaseOnline(String deviceId, int version) {
    this.deviceId = deviceId;
    this.version = version;
  }

  public static BaseOnline createFromPayload(String topic, String[] arguments)
      throws InvalidMessageArgumentException {
    String deviceId = topic.substring(TOPIC_PREFIX.length());

    int version;
    try {
      version = arguments.length >= 2 ? Integer.parseInt(arguments[1]) : 0;
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Invalid version");
    }

    return new BaseOnline(deviceId, version);
  }

  public static BaseOnline create(String deviceId, int version) {
    return new BaseOnline(deviceId, version);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getVersion() {
    return version;
  }

  public static class Factory implements IncomingMessageFactory<BaseOnline> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static Factory create() {
      return new Factory();
    }

    @Override
    public Optional<BaseOnline> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        try {
          return Optional.of(BaseOnline.createFromPayload(topic, arguments));
        } catch (InvalidMessageArgumentException e) {
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }
}
