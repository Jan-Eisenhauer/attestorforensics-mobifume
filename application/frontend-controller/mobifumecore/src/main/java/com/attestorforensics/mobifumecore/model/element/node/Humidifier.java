package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierEnable;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierReset;
import com.attestorforensics.mobifumecore.model.element.misc.HumidifierWaterState;
import com.attestorforensics.mobifumecore.model.element.misc.Led;

public class Humidifier extends Device {

  private static final int WATER_EMPTY_SIGNAL_COUNT_UNTIL_ERROR = 5;

  private boolean humidify;
  private Led led1;
  private Led led2;
  private boolean overHeated;

  private int waterEmptyPingCount;
  private HumidifierWaterState waterState;

  public Humidifier(MessageSender messageSender, final String deviceId, final int version) {
    super(messageSender, DeviceType.HUMIDIFIER, deviceId, version);
  }

  @Override
  public void reset() {
    messageSender.send(HumidifierReset.create(deviceId));
  }

  public void updateHumidify(boolean humidifying) {
    if (humidify == humidifying) {
      return;
    }

    forceUpdateHumidify(humidifying);
  }

  public void forceUpdateHumidify(boolean humidifying) {
    messageSender.send(HumidifierEnable.create(deviceId, humidifying));
  }

  public void setLed1(Led led1) {
    this.led1 = led1;

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

  public boolean isHumidify() {
    return humidify;
  }

  public void setHumidify(boolean humidify) {
    this.humidify = humidify;
  }

  public Led getLed1() {
    return led1;
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
