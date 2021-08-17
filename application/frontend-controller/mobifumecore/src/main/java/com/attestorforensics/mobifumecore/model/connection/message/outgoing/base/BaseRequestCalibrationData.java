package com.attestorforensics.mobifumecore.model.connection.message.outgoing.base;

import com.attestorforensics.mobifumecore.model.connection.message.outgoing.OutgoingMessage;

public class BaseRequestCalibrationData implements OutgoingMessage {

  private static final String TOPIC_PREFIX = "/MOBIfume/base/cmd/";

  private final String deviceId;

  private BaseRequestCalibrationData(String deviceId) {
    this.deviceId = deviceId;
  }

  public static BaseRequestCalibrationData create(String deviceId) {
    return new BaseRequestCalibrationData(deviceId);
  }

  @Override
  public String topic() {
    return TOPIC_PREFIX + deviceId;
  }

  @Override
  public String payload() {
    // unnecessary parameter '1' is a workaround for a bug in the base firmware which requires a
    // parameter
    return "G;1";
  }
}
