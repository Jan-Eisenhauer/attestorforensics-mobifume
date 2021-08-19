package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.base.BaseCalibrationDataUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierUpdatedEvent;
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
    overviewController.updateBase(event.getBase());
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    overviewController.updateHumidifier(event.getHumidifier());
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    overviewController.updateBase(event.getBase());
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    overviewController.updateHumidifier(event.getHumidifier());
  }

  @EventHandler
  public void onBaseUpdated(BaseUpdatedEvent event) {
    overviewController.updateBase(event.getBase());
  }

  @EventHandler
  public void onHumidifierUpdated(HumidifierUpdatedEvent event) {
    overviewController.updateHumidifier(event.getHumidifier());
  }

  @EventHandler
  public void onBaseCalibrationDataUpdated(BaseCalibrationDataUpdatedEvent event) {
    overviewController.updateBase(event.getBase());
  }
}
