package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.item.DeviceItemController;
import com.attestorforensics.mobifume.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifume.controller.item.GroupHumItemController;
import com.attestorforensics.mobifume.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifume.controller.util.ItemErrorType;
import com.attestorforensics.mobifume.model.event.WaterErrorEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

public class WaterErrorListener implements Listener {

  private List<Device> waterError;

  public WaterErrorListener() {
    waterError = new ArrayList<>();
  }

  @EventHandler
  public void onWater(WaterErrorEvent event) {
    switch (event.getStatus()) {
      case FILLED:
        waterError.remove(event.getDevice());
        updateErrors(event.getDevice());
        break;
      case EMPTY:
        waterError.add(event.getDevice());
        updateErrors(event.getDevice());
        break;
      default:
        break;
    }
  }

  void updateErrors(Device device) {
    Platform.runLater(() -> {
      if (waterError.contains(device)) {
        String message = LocaleManager.getInstance().getString("hum.error.water");
        showDeviceItemError(device, message, ItemErrorType.HUMIDIFIER_WATER);
        showGroupHumItemError(device, message, ItemErrorType.HUMIDIFIER_WATER);
      } else {
        hideDeviceItemError(device, ItemErrorType.HUMIDIFIER_WATER);
        hideGroupHumItemError(device, ItemErrorType.HUMIDIFIER_WATER);
      }
    });
  }

  private void showDeviceItemError(Device hum, String message, ItemErrorType errorType) {
    DeviceItemController humController = DeviceItemControllerHolder.getInstance()
        .getController(hum);
    humController.showError(message, true, errorType);
  }

  private void showGroupHumItemError(Device hum, String message, ItemErrorType errorType) {
    GroupHumItemController humController = GroupItemControllerHolder.getInstance()
        .getHumController(hum);
    if (humController == null) {
      return;
    }
    humController.showError(message, true, errorType);
  }

  private void hideDeviceItemError(Device hum, ItemErrorType errorType) {
    DeviceItemController humController = DeviceItemControllerHolder.getInstance()
        .getController(hum);
    humController.hideError(errorType);
  }

  private void hideGroupHumItemError(Device hum, ItemErrorType errorType) {
    GroupHumItemController humController = GroupItemControllerHolder.getInstance()
        .getHumController(hum);
    if (humController == null) {
      return;
    }
    humController.hideError(errorType);
  }
}