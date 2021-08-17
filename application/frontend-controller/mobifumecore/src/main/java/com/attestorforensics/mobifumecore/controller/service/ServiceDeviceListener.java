package com.attestorforensics.mobifumecore.controller.service;

import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class ServiceDeviceListener implements Listener {

  private ServiceController serviceController;

  private ServiceDeviceListener(ServiceController serviceController) {
    this.serviceController = serviceController;
  }

  static ServiceDeviceListener create(ServiceController serviceController) {
    return new ServiceDeviceListener(serviceController);
  }

  @EventHandler
  public void onDeviceUpdate(DeviceConnectionEvent event) {
    switch (event.getStatus()) {
      case CONNECTED:
        serviceController.addDevice(event.getDevice());
        break;
      case DISCONNECTED:
      case LOST:
        serviceController.removeDevice(event.getDevice());
        break;
      case STATUS_UPDATED:
        serviceController.updateDevice(event.getDevice());
        break;
      case CALIBRATION_DATA_UPDATED:
        serviceController.updateCalibration(event.getDevice());
        break;
      default:
        break;
    }
  }
}
