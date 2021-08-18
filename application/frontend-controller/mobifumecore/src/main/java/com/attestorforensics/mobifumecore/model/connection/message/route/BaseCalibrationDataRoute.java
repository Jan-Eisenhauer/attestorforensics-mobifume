package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseCalibrationData;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.event.base.BaseCalibrationDataUpdatedEvent;
import java.util.Optional;

public class BaseCalibrationDataRoute implements MessageRoute<BaseCalibrationData> {

  private final DevicePool devicePool;

  private BaseCalibrationDataRoute(DevicePool devicePool) {
    this.devicePool = devicePool;
  }

  public static BaseCalibrationDataRoute create(DevicePool devicePool) {
    return new BaseCalibrationDataRoute(devicePool);
  }


  @Override
  public Class<BaseCalibrationData> type() {
    return BaseCalibrationData.class;
  }

  @Override
  public void onReceived(BaseCalibrationData message) {
    Optional<Base> optionalBase = devicePool.getBase(message.getDeviceId());
    if (!optionalBase.isPresent()) {
      return;
    }

    Base base = optionalBase.get();
    base.setCalibration(message.getHumidityCalibration().getGradient(),
        message.getHumidityCalibration().getOffset(),
        message.getTemperatureCalibration().getGradient(),
        message.getTemperatureCalibration().getOffset());
    Mobifume.getInstance().getEventDispatcher().call(BaseCalibrationDataUpdatedEvent.create(base));
  }
}
