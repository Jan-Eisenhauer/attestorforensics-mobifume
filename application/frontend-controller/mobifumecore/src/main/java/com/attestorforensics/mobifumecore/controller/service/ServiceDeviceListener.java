package com.attestorforensics.mobifumecore.controller.service;

import com.attestorforensics.mobifumecore.model.event.base.BaseCalibrationDataUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseStatusUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierStatusUpdatedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class ServiceDeviceListener implements Listener {

  private final ServiceController serviceController;

  private ServiceDeviceListener(ServiceController serviceController) {
    this.serviceController = serviceController;
  }

  static ServiceDeviceListener create(ServiceController serviceController) {
    return new ServiceDeviceListener(serviceController);
  }

  @EventHandler
  public void onBaseConnected(BaseConnectedEvent event) {
    serviceController.addDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierConnected(HumidifierConnectedEvent event) {
    serviceController.addDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseDisconnected(BaseDisconnectedEvent event) {
    serviceController.removeDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierDisconnected(HumidifierDisconnectedEvent event) {
    serviceController.removeDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    serviceController.removeDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    serviceController.removeDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseStatusUpdated(BaseStatusUpdatedEvent event) {
    serviceController.updateDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierStatusUpdated(HumidifierStatusUpdatedEvent event) {
    serviceController.updateDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseCalibrationDataUpdated(BaseCalibrationDataUpdatedEvent event) {
    serviceController.updateCalibration(event.getBase());
  }
}
