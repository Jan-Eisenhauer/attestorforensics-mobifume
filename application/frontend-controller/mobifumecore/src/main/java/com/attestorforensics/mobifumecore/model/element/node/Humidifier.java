package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierEnable;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierReset;
import com.attestorforensics.mobifumecore.model.element.misc.Led;
import com.attestorforensics.mobifumecore.model.event.humidifier.error.WaterErrorEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.error.WaterErrorResolvedEvent;

public class Humidifier extends Device {

  private boolean humidify;

  private Led led1;
  private Led led2;
  private boolean overHeated;

  private int waterState;
  private boolean waterEmpty;

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

    // wait for 5 water empty signals
    if (waterState < 5 && led1 == Led.BLINKING) {
      waterState++;
      if (waterState == 5 && !waterEmpty) {
        waterEmpty = true;
        Mobifume.getInstance().getEventDispatcher().call(WaterErrorEvent.create(this));
      }
    } else if (waterState > 0) {
      waterState--;
      if (waterState == 0 && waterEmpty) {
        waterEmpty = false;
        Mobifume.getInstance().getEventDispatcher().call(WaterErrorResolvedEvent.create(this));
      }
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

  public int getWaterState() {
    return waterState;
  }

  public boolean isWaterEmpty() {
    return waterEmpty;
  }
}
