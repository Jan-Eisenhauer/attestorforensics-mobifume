package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.base.BaseConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierConnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
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
    overviewController.addBase(event.getBase());
  }

  @EventHandler
  public void onHumidifierConnected(HumidifierConnectedEvent event) {
    overviewController.addHumidifier(event.getHumidifier());
  }

  @EventHandler
  public void onBaseDisconnected(BaseDisconnectedEvent event) {
    overviewController.removeBase(event.getBase());
  }

  @EventHandler
  public void onHumidifierDisconnected(HumidifierDisconnectedEvent event) {
    overviewController.removeHumidifier(event.getHumidifier());
  }
}
