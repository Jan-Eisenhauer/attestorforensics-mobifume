package com.attestorforensics.mobifumecore.model.connection.message.incoming.base;

import com.attestorforensics.mobifumecore.model.connection.message.InvalidMessageArgumentException;
import com.attestorforensics.mobifumecore.model.connection.message.MessagePattern;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessage;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.IncomingMessageFactory;
import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.regex.Pattern;

public class BaseCalibrationData implements IncomingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/status/";
  private static final String FIRST_ARGUMENT = "S";
  private static final String SECOND_ARGUMENT = "CALIB_DATA";

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
    if (arguments.length < 6) {
      throw new InvalidMessageArgumentException("Not enough arguments provided");
    }

    String deviceId = topic.substring(TOPIC_PREFIX.length());

    Calibration humidityCalibration;
    Calibration temperatureCalibration;
    try {
      humidityCalibration =
          Calibration.create(Float.parseFloat(arguments[2]), Float.parseFloat(arguments[3]));
      temperatureCalibration =
          Calibration.create(Float.parseFloat(arguments[4]), Float.parseFloat(arguments[5]));
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
        MessagePattern.create(Pattern.compile(TOPIC_PREFIX + ".+"),
            ImmutableMap.of(0, Pattern.compile(FIRST_ARGUMENT), 1,
                Pattern.compile(SECOND_ARGUMENT)));

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
