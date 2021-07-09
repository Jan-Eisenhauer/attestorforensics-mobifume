package com.attestorforensics.mobifumecore.model.connection.message.incoming.humidifier;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import java.util.Optional;

public class HumidifierPing implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/hum/status/";
  private static final String FIRST_ARGUMENT = "P";

  private final String deviceId;
  private final int rssi;
  private final boolean humidifying;
  private final int led1;
  private final int led2;
  private final boolean overHeated;

  private HumidifierPing(String deviceId, int rssi, boolean humidifying, int led1, int led2,
      boolean overHeated) {
    this.deviceId = deviceId;
    this.rssi = rssi;
    this.humidifying = humidifying;
    this.led1 = led1;
    this.led2 = led2;
    this.overHeated = overHeated;
  }

  public static HumidifierPing createFromPayload(String topic, String[] arguments)
      throws InvalidMessageArgumentException {
    if (arguments.length < 5) {
      throw new InvalidMessageArgumentException("Not enough arguments provided");
    }

    String deviceId = topic.substring(TOPIC_PREFIX.length());

    int rssi;
    boolean humidifying;
    int led1;
    int led2;
    boolean overHeated;

    try {
      rssi = Integer.parseInt(arguments[1]);
      humidifying = Integer.parseInt(arguments[2]) == 1;
      led1 = Integer.parseInt(arguments[3]);
      led2 = Integer.parseInt(arguments[4]);
      // TODO - enum for led state
      overHeated = arguments.length >= 6 && Boolean.parseBoolean(arguments[5]);
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Cannot convert arguments to base ping");
    }

    return new HumidifierPing(deviceId, rssi, humidifying, led1, led2, overHeated);
  }

  public static HumidifierPing create(String deviceId, int rssi, boolean humidifying, int led1,
      int led2, boolean overHeated) {
    return new HumidifierPing(deviceId, rssi, humidifying, led1, led2, overHeated);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getRssi() {
    return rssi;
  }

  public boolean isHumidifying() {
    return humidifying;
  }

  public int getLed1() {
    return led1;
  }

  public int getLed2() {
    return led2;
  }

  public boolean isOverHeated() {
    return overHeated;
  }

  public static class Factory implements IncomingMessageFactory<HumidifierPing> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static HumidifierPing.Factory create() {
      return new HumidifierPing.Factory();
    }

    @Override
    public Optional<HumidifierPing> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        try {
          return Optional.of(HumidifierPing.createFromPayload(topic, arguments));
        } catch (InvalidMessageArgumentException e) {
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }
}
