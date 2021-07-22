package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import java.util.Optional;

public class BaseCalibrationData implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "CALIB_DATA";

  private final String deviceId;
  private final Calibration humidityCalibration;
  private final Calibration temperatureCalibration;

  private BaseCalibrationData(String deviceId, Calibration humidityCalibration,
      Calibration temperatureCalibration) {
    this.deviceId = deviceId;
    this.humidityCalibration = humidityCalibration;
    this.temperatureCalibration = temperatureCalibration;
  }

  public static BaseCalibrationData createFromPayload(String topic, String[] arguments)
      throws InvalidMessageArgumentException {
    if (arguments.length < 5) {
      throw new InvalidMessageArgumentException("Not enough arguments provided");
    }

    String deviceId = topic.substring(TOPIC_PREFIX.length());

    Calibration humidityCalibration;
    Calibration temperatureCalibration;
    try {
      humidityCalibration =
          Calibration.create(Float.parseFloat(arguments[1]), Float.parseFloat(arguments[2]));
      temperatureCalibration =
          Calibration.create(Float.parseFloat(arguments[3]), Float.parseFloat(arguments[4]));
    } catch (NumberFormatException e) {
      throw new InvalidMessageArgumentException("Cannot convert arguments to calibration data");
    }

    return new BaseCalibrationData(deviceId, humidityCalibration, temperatureCalibration);
  }

  public static BaseCalibrationData create(String deviceId, Calibration humidityCalibration,
      Calibration temperatureCalibration) {
    return new BaseCalibrationData(deviceId, humidityCalibration, temperatureCalibration);
  }

  public String getDeviceId() {
    return deviceId;
  }

  public Calibration getHumidityCalibration() {
    return humidityCalibration;
  }

  public Calibration getTemperatureCalibration() {
    return temperatureCalibration;
  }

  public static class Factory implements IncomingMessageFactory<BaseCalibrationData> {

    private final MessagePattern messagePattern =
        MessagePattern.createSingleArgumentPattern(TOPIC_PREFIX + ".+", FIRST_ARGUMENT);

    public static BaseCalibrationData.Factory create() {
      return new BaseCalibrationData.Factory();
    }

    @Override
    public Optional<BaseCalibrationData> create(String topic, String[] arguments) {
      if (messagePattern.matches(topic, arguments)) {
        try {
          return Optional.of(BaseCalibrationData.createFromPayload(topic, arguments));
        } catch (InvalidMessageArgumentException e) {
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }
}
