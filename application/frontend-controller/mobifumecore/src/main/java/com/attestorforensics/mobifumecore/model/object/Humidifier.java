package com.attestorforensics.mobifumecore.model.object;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.ClientConnection;
import com.attestorforensics.mobifumecore.model.event.WaterErrorEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Humidifier extends Device {

  @Setter
  private boolean humidify;

  // led status: 0->off; 1->on; 2->blinking
  private int led1;
  @Setter
  private int led2;
  @Setter
  private boolean overTemperature;

  private int waterState;
  private boolean waterEmpty;

  public Humidifier(ClientConnection clientConnection, final String id, final int version) {
    super(clientConnection, DeviceType.HUMIDIFIER, id, version);
  }

  public void updateHumidify(boolean humidifying) {
    if (humidify == humidifying) {
      return;
    }

    forceUpdateHumidify(humidifying);
  }

  public void forceUpdateHumidify(boolean humidifying) {
    getEncoder().humEnable(this, humidifying);
  }

  public void setLed1(int led1) {
    this.led1 = led1;

    // wait for 5 water empty signals
    if (waterState < 5 && led1 == 2) {
      waterState++;
      if (waterState == 5 && !waterEmpty) {
        waterEmpty = true;
        Mobifume.getInstance()
            .getEventManager()
            .call(new WaterErrorEvent(this, WaterErrorEvent.WaterStatus.EMPTY));
      }
    } else if (waterState > 0) {
      waterState--;
      if (waterState == 0 && waterEmpty) {
        waterEmpty = false;
        Mobifume.getInstance()
            .getEventManager()
            .call(new WaterErrorEvent(this, WaterErrorEvent.WaterStatus.FILLED));
      }
    }
  }
}
