package com.attestorforensics.mobifumecore.model.connection.message.route;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.ModelManager;
import com.attestorforensics.mobifumecore.model.connection.message.incoming.base.BaseCalibrationData;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;

public class BaseCalibrationDataRoute implements MessageRoute<BaseCalibrationData> {

  private final ModelManager modelManager;

  private BaseCalibrationDataRoute(ModelManager modelManager) {
    this.modelManager = modelManager;
  }

  public static BaseCalibrationDataRoute create(ModelManager modelManager) {
    return new BaseCalibrationDataRoute(modelManager);
  }


  @Override
  public Class<BaseCalibrationData> type() {
    return BaseCalibrationData.class;
  }

  @Override
  public void onReceived(BaseCalibrationData message) {
    Device device = modelManager.getDevice(message.getDeviceId());
    if (device == null) {
      return;
    }

    if (device.getType() != DeviceType.BASE) {
      return;
    }

    Base base = (Base) device;
    base.setCalibration(message.getHumidityCalibration().getGradient(),
        message.getHumidityCalibration().getOffset(),
        message.getTemperatureCalibration().getGradient(),
        message.getTemperatureCalibration().getOffset());
    Mobifume.getInstance()
        .getEventDispatcher()
        .call(new DeviceConnectionEvent(device,
            DeviceConnectionEvent.DeviceStatus.CALIBRATION_DATA_UPDATED));
  }
}
