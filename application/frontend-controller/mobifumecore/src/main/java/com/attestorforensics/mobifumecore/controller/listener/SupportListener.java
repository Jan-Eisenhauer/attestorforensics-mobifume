package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.SupportController;
import com.attestorforensics.mobifumecore.controller.item.SupportBaseItemController;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class SupportListener implements Listener {

  private SupportController supportController;

  public SupportListener(SupportController supportController) {
    this.supportController = supportController;
  }

  @EventHandler
  public void onDeviceUpdate(DeviceConnectionEvent event) {
    Platform.runLater(() -> {
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
        case CALIBRATION_DATA_UPDATED:
          ((SupportBaseItemController) supportController.getSupportItemController(
              event.getDevice())).updateCalibration();
          break;
        default:
          break;
      }
    });
  }
}
