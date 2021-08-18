package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.controller.item.DeviceItemController;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.item.GroupHumItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.event.humidifier.error.WaterErrorEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.error.WaterErrorResolvedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;

public class WaterErrorListener implements Listener {

  // TODO - refactor listener

  private List<Device> waterError;

  public WaterErrorListener() {
    waterError = new ArrayList<>();
  }

  @EventHandler
  public void onWaterError(WaterErrorEvent event) {
    waterError.add(event.getHumidifier());
    updateErrors(event.getHumidifier());
  }

  @EventHandler
  public void onWaterResolved(WaterErrorResolvedEvent event) {
    waterError.remove(event.getHumidifier());
    updateErrors(event.getHumidifier());
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
    DeviceItemController humController =
        DeviceItemControllerHolder.getInstance().getController(hum);
    humController.showError(message, true, errorType);
  }

  private void showGroupHumItemError(Device hum, String message, ItemErrorType errorType) {
    GroupHumItemController humController =
        GroupItemControllerHolder.getInstance().getHumController(hum);
    if (humController == null) {
      return;
    }
    humController.showError(message, true, errorType);
  }

  private void hideDeviceItemError(Device hum, ItemErrorType errorType) {
    DeviceItemController humController =
        DeviceItemControllerHolder.getInstance().getController(hum);
    humController.hideError(errorType);
  }

  private void hideGroupHumItemError(Device hum, ItemErrorType errorType) {
    GroupHumItemController humController =
        GroupItemControllerHolder.getInstance().getHumController(hum);
    if (humController == null) {
      return;
    }
    humController.hideError(errorType);
  }
}
