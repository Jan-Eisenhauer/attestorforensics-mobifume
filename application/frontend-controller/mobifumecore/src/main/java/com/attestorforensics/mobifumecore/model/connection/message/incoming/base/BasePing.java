package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import java.util.Optional;

public class BasePing implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "P";

  private final String deviceId;
  private final int rssi;
  private final double temperature;
  private final double humidity;
  private final double heaterSetpoint;
  private final double heaterTemperature;
  private final int latch;

  private BasePing(String deviceId, int rssi, double temperature, double humidity,
      double heaterSetpoint, double heaterTemperature, int latch) {
    this.deviceId = deviceId;
    this.rssi = rssi;
    this.temperature = temperature;
    this.humidity = humidity;
    this.heaterSetpoint = heaterSetpoint;
    this.heaterTemperature = heaterTemperature;
    this.latch = latch;
  }

  public static BasePing createFromPayload(String topic, String[] arguments)
      throws InvalidMessageArgumentException {
    if (arguments.length < 7) {
      throw new InvalidMessageArgumentException("Not enough arguments provided");
    }

    String deviceId = topic.substring(TOPIC_PREFIX.length());

    int rssi;
    double temperature;
    double humidity;
    double heaterSetpoint;
    double heaterTemperature;
    int latch;

    try {
      rssi = Integer.parseInt(arguments[1]);
      temperature = Double.parseDouble(arguments[2]);
      humidity = Double.parseDouble(arguments[3]);
      heaterSetpoint = Double.parseDouble(arguments[4]);
      heaterTemperature = Double.parseDouble(arguments[5]);
      latch = Integer.parseInt(arguments[6]);
      // TODO - enum for latch state
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Cannot convert arguments to base ping");
    }

    return new BasePing(deviceId, rssi, temperature, humidity, heaterSetpoint, heaterTemperature,
        latch);
  }

  public static BasePing create(String deviceId, int rssi, double temperature, double humidity,
      double heaterSetpoint, double heaterTemperature, int latch) {
    return new BasePing(deviceId, rssi, temperature, humidity, heaterSetpoint, heaterTemperature,
        latch);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getRssi() {
    return rssi;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getHumidity() {
    return humidity;
  }

  public double getHeaterSetpoint() {
    return heaterSetpoint;
  }

  public double getHeaterTemperature() {
    return heaterTemperature;
  }

  public int getLatch() {
    return latch;
  }

  public static class Factory implements IncomingMessageFactory<BasePing> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static BasePing.Factory create() {
      return new BasePing.Factory();
    }

    @Override
    public Optional<BasePing> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        try {
          return Optional.of(BasePing.createFromPayload(topic, arguments));
        } catch (InvalidMessageArgumentException e) {
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }
}
