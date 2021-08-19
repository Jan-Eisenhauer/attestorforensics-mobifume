package com.attestorforensics.mobifumecore.controller.overview;

import com.attestorforensics.mobifumecore.controller.item.DeviceItemController;
import com.attestorforensics.mobifumecore.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class OverviewDeviceConnectionListener implements Listener {

  private OverviewDeviceConnectionListener() {
  }

  static OverviewDeviceConnectionListener create() {
    return new OverviewDeviceConnectionListener();
  }


  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    Platform.runLater(() -> showLostError(event.getBase()));
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    Platform.runLater(() -> showLostError(event.getHumidifier()));
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    Platform.runLater(() -> hideLostError(event.getBase()));
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    Platform.runLater(() -> hideLostError(event.getHumidifier()));
  }

  private void showLostError(Device device) {
    String message = LocaleManager.getInstance().getString("device.error.connection");

    DeviceItemController deviceController =
        DeviceItemControllerHolder.getInstance().getController(device);
    if (deviceController != null) {
      deviceController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    }
  }

  private void hideLostError(Device device) {
    DeviceItemController deviceController =
        DeviceItemControllerHolder.getInstance().getController(device);
    if (deviceController != null) {
      deviceController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
    }
  }
}
