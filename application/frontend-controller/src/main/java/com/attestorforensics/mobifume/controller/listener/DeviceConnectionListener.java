package com.attestorforensics.mobifume.controller.listener;

import com.attestorforensics.mobifume.controller.OverviewController;
import com.attestorforensics.mobifume.controller.item.DeviceItemController;
import com.attestorforensics.mobifume.controller.item.DeviceItemControllerHolder;
import com.attestorforensics.mobifume.controller.item.GroupBaseItemController;
import com.attestorforensics.mobifume.controller.item.GroupHumItemController;
import com.attestorforensics.mobifume.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifume.controller.util.ItemErrorType;
import com.attestorforensics.mobifume.model.event.DeviceConnectionEvent;
import com.attestorforensics.mobifume.model.listener.EventHandler;
import com.attestorforensics.mobifume.model.listener.Listener;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.DeviceType;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import javafx.application.Platform;

public class DeviceConnectionListener implements Listener {

  private final OverviewController controller;

  public DeviceConnectionListener(OverviewController controller) {
    this.controller = controller;
  }

  @EventHandler
  public void onNodeConnection(DeviceConnectionEvent event) {
    Platform.runLater(() -> {
      switch (event.getStatus()) {
        case CONNECTED:
          controller.addNode(event.getDevice());
          break;
        case DISCONNECTED:
          controller.removeNode(event.getDevice());
          break;
        case LOST:
          controller.updateNode(event.getDevice());
          showLostError(event.getDevice());
          break;
        case STATUS_UPDATED:
          controller.updateNode(event.getDevice());
          break;
        case RECONNECT:
          controller.updateNode(event.getDevice());
          hideLostError(event.getDevice());
          break;
        default:
          break;
      }
    });
  }

  private void showLostError(Device device) {
    String message = LocaleManager.getInstance().getString("device.error.connection");

    // overview error
    DeviceItemController deviceController = DeviceItemControllerHolder.getInstance()
        .getController(device);
    if (deviceController != null) {
      deviceController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    }

    // group error
    if (device.getType() == DeviceType.BASE) {
      GroupBaseItemController baseController = GroupItemControllerHolder.getInstance()
          .getBaseController(device);
      if (baseController != null) {
        baseController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    } else if (device.getType() == DeviceType.HUMIDIFIER) {
      GroupHumItemController humController = GroupItemControllerHolder.getInstance()
          .getHumController(device);
      if (humController != null) {
        humController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    }
  }

  private void hideLostError(Device device) {
    // overview error
    DeviceItemController deviceController = DeviceItemControllerHolder.getInstance()
        .getController(device);
    if (deviceController != null) {
      deviceController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
    }

    // group error
    if (device.getType() == DeviceType.BASE) {
      GroupBaseItemController baseController = GroupItemControllerHolder.getInstance()
          .getBaseController(device);
      if (baseController != null) {
        baseController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    } else if (device.getType() == DeviceType.HUMIDIFIER) {
      GroupHumItemController humController = GroupItemControllerHolder.getInstance()
          .getHumController(device);
      if (humController != null) {
        humController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    }
  }
}
