package com.attestorforensics.mobifumecore.controller.overview.item;

import com.attestorforensics.mobifumecore.model.event.base.BaseDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierDisconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierUpdatedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class DeviceItemConnectionListener implements Listener {

  private final DeviceItemController deviceItemController;

  private DeviceItemConnectionListener(DeviceItemController deviceItemController) {
    this.deviceItemController = deviceItemController;
  }

  static DeviceItemConnectionListener create(DeviceItemController deviceItemController) {
    return new DeviceItemConnectionListener(deviceItemController);
  }

  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    if (event.getBase() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    if (event.getHumidifier() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    if (event.getBase() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    if (event.getHumidifier() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onBaseUpdated(BaseUpdatedEvent event) {
    if (event.getBase() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onHumidifierUpdated(HumidifierUpdatedEvent event) {
    if (event.getHumidifier() != deviceItemController.getDevice()) {
      return;
    }

    Platform.runLater(deviceItemController::updateDevice);
  }

  @EventHandler
  public void onBaseDisconnected(BaseDisconnectedEvent event) {
    if (event.getBase() != deviceItemController.getDevice()) {
      return;
    }


    deviceItemController.onRemove();
  }

  @EventHandler
  public void onHumidifierDisconnected(HumidifierDisconnectedEvent event) {
    if (event.getHumidifier() != deviceItemController.getDevice()) {
      return;
    }

    deviceItemController.onRemove();
  }
}
