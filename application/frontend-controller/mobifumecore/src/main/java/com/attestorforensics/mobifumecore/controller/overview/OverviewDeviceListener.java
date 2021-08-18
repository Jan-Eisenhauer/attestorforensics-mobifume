package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.base.BaseCalibrationDataUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseStatusUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierStatusUpdatedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class OverviewDeviceListener implements Listener {

  private final OverviewController overviewController;

  private OverviewDeviceListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  static OverviewDeviceListener create(OverviewController overviewController) {
    return new OverviewDeviceListener(overviewController);
  }

  @EventHandler
  public void onBaseConnected(BaseConnectedEvent event) {
    overviewController.addDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierConnected(HumidifierConnectedEvent event) {
    overviewController.addDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseDisconnected(BaseDisconnectedEvent event) {
    overviewController.removeDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierDisconnected(HumidifierDisconnectedEvent event) {
    overviewController.removeDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    overviewController.updateDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    overviewController.updateDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    overviewController.updateDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    overviewController.updateDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseStatusUpdated(BaseStatusUpdatedEvent event) {
    overviewController.updateDevice(event.getBase());
  }

  @EventHandler
  public void onHumidifierStatusUpdated(HumidifierStatusUpdatedEvent event) {
    overviewController.updateDevice(event.getHumidifier());
  }

  @EventHandler
  public void onBaseCalibrationDataUpdated(BaseCalibrationDataUpdatedEvent event) {
    overviewController.updateDevice(event.getBase());
  }
}
