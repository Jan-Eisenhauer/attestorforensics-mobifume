package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

class OverviewDeviceListener implements Listener {

  private final OverviewController overviewController;

  private OverviewDeviceListener(OverviewController overviewController) {
    this.overviewController = overviewController;
  }

  static OverviewDeviceListener create(OverviewController overviewController) {
    return new OverviewDeviceListener(overviewController);
  }

  @EventHandler
  public void onDevice(DeviceConnectionEvent event) {
    switch (event.getStatus()) {
      case CONNECTED:
        overviewController.addDevice(event.getDevice());
        break;
      case DISCONNECTED:
        overviewController.removeDevice(event.getDevice());
        break;
      default:
        overviewController.updateDevice(event.getDevice());
        break;
    }
  }
}
