package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierEnable;
import com.attestorforensics.mobifumecore.model.connection.message.outgoing.humidifier.HumidifierReset;
import com.attestorforensics.mobifumecore.model.event.WaterErrorEvent;

public class Humidifier extends Device {

  private boolean humidify;

  // led status: 0->off; 1->on; 2->blinking
  private int led1;
  private int led2;
  private boolean overTemperature;

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

  public void setLed1(int led1) {
    this.led1 = led1;

    // wait for 5 water empty signals
    if (waterState < 5 && led1 == 2) {
      waterState++;
      if (waterState == 5 && !waterEmpty) {
        waterEmpty = true;
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(new WaterErrorEvent(this, WaterErrorEvent.WaterStatus.EMPTY));
      }
    } else if (waterState > 0) {
      waterState--;
      if (waterState == 0 && waterEmpty) {
        waterEmpty = false;
        Mobifume.getInstance()
            .getEventDispatcher()
            .call(new WaterErrorEvent(this, WaterErrorEvent.WaterStatus.FILLED));
      }
    }
  }

  public boolean isHumidify() {
    return humidify;
  }

  public void setHumidify(boolean humidify) {
    this.humidify = humidify;
  }

  public int getLed1() {
    return led1;
  }

  public int getLed2() {
    return led2;
  }

  public void setLed2(int led2) {
    this.led2 = led2;
  }

  public boolean isOverTemperature() {
    return overTemperature;
  }

  public void setOverTemperature(boolean overTemperature) {
    this.overTemperature = overTemperature;
  }

  public int getWaterState() {
    return waterState;
  }

  public boolean isWaterEmpty() {
    return waterEmpty;
  }
}
