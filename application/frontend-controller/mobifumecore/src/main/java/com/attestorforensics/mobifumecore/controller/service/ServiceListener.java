package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.service.ServiceController;
import com.attestorforensics.mobifumecore.controller.item.ServiceBaseItemController;
import com.attestorforensics.mobifumecore.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class ServiceListener implements Listener {

  private ServiceController serviceController;

  public ServiceListener(ServiceController serviceController) {
    this.serviceController = serviceController;
  }

  @EventHandler
  public void onDeviceUpdate(DeviceConnectionEvent event) {
    Platform.runLater(() -> {
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
          ((ServiceBaseItemController) serviceController.getServiceItemController(
              event.getDevice())).updateCalibration();
          break;
        default:
          break;
      }
    });
  }
}
