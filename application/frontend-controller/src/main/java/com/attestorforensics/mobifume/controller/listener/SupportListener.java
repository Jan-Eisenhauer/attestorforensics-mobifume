package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.SupportController;
import com.attestorforensics.mobifume.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;

public class SupportListener implements Listener {

  private SupportController supportController;

  public SupportListener(SupportController supportController) {
    this.supportController = supportController;
  }

  @EventHandler
  public void onDeviceUpdate(DeviceConnectionEvent event) {
    switch (event.getStatus()) {
      case CONNECTED:
        supportController.addDevice(event.getDevice());
        break;
      case DISCONNECTED:
      case LOST:
        supportController.removeDevice(event.getDevice());
        break;
      case STATUS_UPDATED:
        supportController.updateDevice(event.getDevice());
        break;
      default:
        break;
    }
  }
}
