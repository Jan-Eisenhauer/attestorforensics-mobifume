package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BasePing;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.misc.Latch;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.BaseErrorEvent;
import com.attestorforensics.mobifumecore.model.event.BaseErrorResolvedEvent;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import java.util.Optional;

public class BasePingRoute implements MessageRoute<BasePing> {

  private final DevicePool devicePool;
  private final GroupPool groupPool;

  private BasePingRoute(DevicePool devicePool, GroupPool groupPool) {
    this.devicePool = devicePool;
    this.groupPool = groupPool;
  }

  public static BasePingRoute create(DevicePool devicePool, GroupPool groupPool) {
    return new BasePingRoute(devicePool, groupPool);
  }

  @Override
  public Class<BasePing> type() {
    return BasePing.class;
  }

  @Override
  public void onReceived(BasePing message) {
    Optional<Base> optionalBase = devicePool.getBase(message.getDeviceId());
    if (!optionalBase.isPresent()) {
      return;
    }

    Base base = optionalBase.get();
    base.setRssi(message.getRssi());

    updateTemperature(message, base);
    updateHumidity(message, base);
    base.setHeaterSetpoint(message.getHeaterSetpoint());
    updateHeaterTemperature(message, base);
    updateLatch(message, base);

    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(base, DeviceConnectionEvent.DeviceStatus.STATUS_UPDATED));

    Optional<Group> optionalGroup = groupPool.getGroupOfBase(base);
    if (optionalGroup.isPresent()) {
      Group group = optionalGroup.get();
      CustomLogger.logGroupBase(group, base);
      group.updateHumidify();
    }
  }

  private void updateTemperature(BasePing message, Base base) {
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
  }

  private void updateHumidity(BasePing message, Base base) {
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
  }

  private void updateHeaterTemperature(BasePing message, Base base) {
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
  }

  private void updateLatch(BasePing message, Base base) {
    Latch latch = message.getLatch();
    Latch oldLatch = base.getLatch();
    base.setLatch(latch);
    if ((latch == Latch.ERROR_OTHER || latch == Latch.ERROR_NOT_REACHED
        || latch == Latch.ERROR_BLOCKED) && (oldLatch != Latch.ERROR_OTHER
        && oldLatch != Latch.ERROR_NOT_REACHED && oldLatch != Latch.ERROR_BLOCKED)) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }

    if ((latch != Latch.ERROR_OTHER && latch != Latch.ERROR_NOT_REACHED
        && latch != Latch.ERROR_BLOCKED) && (oldLatch == Latch.ERROR_OTHER
        || oldLatch == Latch.ERROR_NOT_REACHED || oldLatch == Latch.ERROR_BLOCKED)) {
      Mobifume.getInstance()
          .getEventDispatcher()
          .call(new BaseErrorResolvedEvent(base, BaseErrorEvent.ErrorType.LATCH));
    }
  }
}
