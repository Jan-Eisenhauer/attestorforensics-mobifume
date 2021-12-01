package com.attestorforensics.mobifumecore.model.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierReset;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierToggle;
import com.attestorforensics.mobifumecore.model.node.misc.HumidifierWaterState;
import com.attestorforensics.mobifumecore.model.node.misc.Led;

public class Humidifier extends Device {

  private static final int WATER_EMPTY_SIGNAL_COUNT_UNTIL_ERROR = 5;

  private boolean humidifying;
  private Led led1;
  private Led led2;
  private boolean overHeated;

  private int waterEmptyPingCount;
  private HumidifierWaterState waterState;

  private Humidifier(MessageSender messageSender, String deviceId, int version) {
    super(messageSender, deviceId, version);
  }

  public static Humidifier create(MessageSender messageSender, String deviceId, int version) {
    return new Humidifier(messageSender, deviceId, version);
  }

  public void sendReset() {
    messageSender.send(HumidifierReset.create(deviceId));
  }

  public void sendHumidifyEnable() {
    if (!humidifying) {
      forceSendHumidifyEnable();
    }
  }

  public void sendHumidifyDisable() {
    if (humidifying) {
      forceSendHumidifyDisable();
    }
  }

  public void forceSendHumidifyEnable() {
    messageSender.send(HumidifierToggle.enable(deviceId));
  }

  public void forceSendHumidifyDisable() {
    messageSender.send(HumidifierToggle.disable(deviceId));
  }

  public boolean isHumidifying() {
    return humidifying;
  }

  public void setHumidifying(boolean humidifying) {
    this.humidifying = humidifying;
  }

  public Led getLed1() {
    return led1;
  }

  public void setLed1(Led led1) {
    this.led1 = led1;

    // when multiple blinking messages were received, set water state to empty
    if (led1 == Led.BLINKING && waterEmptyPingCount < WATER_EMPTY_SIGNAL_COUNT_UNTIL_ERROR) {
      waterEmptyPingCount++;
    } else if (led1 != Led.BLINKING && waterEmptyPingCount > 0) {
      waterEmptyPingCount--;
    }

    if (waterEmptyPingCount == WATER_EMPTY_SIGNAL_COUNT_UNTIL_ERROR
        && waterState != HumidifierWaterState.EMPTY) {
      waterState = HumidifierWaterState.EMPTY;
    }

    if (waterEmptyPingCount == 0 && waterState != HumidifierWaterState.FILLED) {
      waterState = HumidifierWaterState.FILLED;
    }
  }

  public Led getLed2() {
    return led2;
  }

  public void setLed2(Led led2) {
    this.led2 = led2;
  }

  public boolean isOverHeated() {
    return overHeated;
  }

  public void setOverHeated(boolean overHeated) {
    this.overHeated = overHeated;
  }

  public HumidifierWaterState getWaterState() {
    return waterState;
  }
}
