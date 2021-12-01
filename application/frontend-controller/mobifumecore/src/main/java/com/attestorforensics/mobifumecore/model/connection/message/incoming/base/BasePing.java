package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.node.misc.BaseLatch;
import com.attestorforensics.mobifumecore.model.node.misc.DoubleSensor;
import java.util.Optional;

public class BasePing implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "P";

  private final String deviceId;
  private final int rssi;
  private final DoubleSensor temperature;
  private final DoubleSensor humidity;
  private final double heaterSetpoint;
  private final DoubleSensor heaterTemperature;
  private final BaseLatch baseLatch;

  private BasePing(String deviceId, int rssi, double temperature, double humidity,
      double heaterSetpoint, double heaterTemperature, BaseLatch baseLatch) {
    this.deviceId = deviceId;
    this.rssi = rssi;
    this.temperature = DoubleSensor.of(temperature);
    this.humidity = DoubleSensor.of(humidity);
    this.heaterSetpoint = heaterSetpoint;
    this.heaterTemperature = DoubleSensor.of(heaterTemperature);
    this.baseLatch = baseLatch;
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
    int latchValue;

    try {
      rssi = Integer.parseInt(arguments[1]);
      temperature = Double.parseDouble(arguments[2]);
      humidity = Double.parseDouble(arguments[3]);
      heaterSetpoint = Double.parseDouble(arguments[4]);
      heaterTemperature = Double.parseDouble(arguments[5]);
      latchValue = Integer.parseInt(arguments[6]);
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Cannot convert arguments to base ping");
    }

    BaseLatch baseLatch;
    switch (latchValue) {
      case 0:
        baseLatch = BaseLatch.PURGING;
        break;
      case 1:
        baseLatch = BaseLatch.CIRCULATING;
        break;
      case -1:
        baseLatch = BaseLatch.MOVING;
        break;
      case 2:
        baseLatch = BaseLatch.ERROR_OTHER;
        break;
      case 3:
        baseLatch = BaseLatch.ERROR_NOT_REACHED;
        break;
      case 4:
        baseLatch = BaseLatch.ERROR_BLOCKED;
        break;
      default:
        baseLatch = BaseLatch.UNKNOWN;
        break;
    }

    return new BasePing(deviceId, rssi, temperature, humidity, heaterSetpoint, heaterTemperature,
        baseLatch);
  }

  public static BasePing create(String deviceId, int rssi, double temperature, double humidity,
      double heaterSetpoint, double heaterTemperature, BaseLatch baseLatch) {
    return new BasePing(deviceId, rssi, temperature, humidity, heaterSetpoint, heaterTemperature,
        baseLatch);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public int getRssi() {
    return rssi;
  }

  public DoubleSensor getTemperature() {
    return temperature;
  }

  public DoubleSensor getHumidity() {
    return humidity;
  }

  public double getHeaterSetpoint() {
    return heaterSetpoint;
  }

  public DoubleSensor getHeaterTemperature() {
    return heaterTemperature;
  }

  public BaseLatch getLatch() {
    return baseLatch;
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
