package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BasePing;
import com.attestorforensics.mobifumecore.model.element.group.Room;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.event.BaseErrorEvent;
import com.attestorforensics.mobifumecore.model.event.BaseErrorResolvedEvent;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;

public class BasePingRoute implements MessageRoute<BasePing> {

  private final ModelManager modelManager;

  private BasePingRoute(ModelManager modelManager) {
    this.modelManager = modelManager;
  }

  public static BasePingRoute create(ModelManager modelManager) {
    return new BasePingRoute(modelManager);
  }


  @Override
  public Class<BasePing> type() {
    return BasePing.class;
  }

  @Override
  public void onReceived(BasePing message) {
    Device device = modelManager.getDevice(message.getDeviceId());
    if (device == null) {
      return;
    }
    if (device.getType() != DeviceType.BASE) {
      return;
    }
    Base base = (Base) device;
    base.setRssi(message.getRssi());

    double temperature = message.getTemperature();
    double oldTemperature = base.getTemperature();
    base.setTemperature(temperature);
    if (temperature == -128 && oldTemperature != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.TEMPERATURE));
    }

    if (oldTemperature == -128 && temperature != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.TEMPERATURE));
    }

    double humidity = message.getHumidity();
    double oldHumidity = base.getHumidity();
    base.setHumidity(humidity);
    if (humidity == -128 && oldHumidity != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.HUMIDITY));
    }

    if (oldHumidity == -128 && humidity != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.HUMIDITY));
    }

    base.setHeaterSetpoint(message.getHeaterSetpoint());

    double heaterTemperature = message.getHeaterTemperature();
    double oldHeaterTemperature = base.getHeaterTemperature();
    base.setHeaterTemperature(heaterTemperature);
    if (heaterTemperature == -128 && oldHeaterTemperature != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.HEATER));
    }
    if (oldHeaterTemperature == -128 && heaterTemperature != -128) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.HEATER));
    }

    int latch = message.getLatch();
    int oldLatch = base.getLatch();
    base.setLatch(latch);
    if ((latch == 3 || latch == 4) && (oldLatch != 3 && oldLatch != 4)) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }

    if ((latch == 0 || latch == 1) && (oldLatch == 3 || oldLatch == 4)) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device, DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Room group = (Room) modelManager.getGroup(base);
    if (group == null) {
      return;
    }

    CustomLogger.logGroupBase(group, base);
    group.updateHumidify();
  }
}
