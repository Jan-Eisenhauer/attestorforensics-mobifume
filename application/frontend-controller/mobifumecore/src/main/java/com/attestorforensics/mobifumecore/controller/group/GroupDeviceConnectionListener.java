package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.controller.item.GroupBaseItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupHumItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class GroupDeviceConnectionListener implements Listener {

  private final GroupController groupController;

  private GroupDeviceConnectionListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static GroupDeviceConnectionListener create(GroupController groupController) {
    return new GroupDeviceConnectionListener(groupController);
  }

  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    if (!groupController.getGroup().containsBase(event.getBase())) {
      return;
    }

    Platform.runLater(() -> showLostError(event.getBase()));
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    if (!groupController.getGroup().containsHumidifier(event.getHumidifier())) {
      return;
    }

    Platform.runLater(() -> showLostError(event.getHumidifier()));
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    if (!groupController.getGroup().containsBase(event.getBase())) {
      return;
    }

    Platform.runLater(() -> hideLostError(event.getBase()));
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    if (!groupController.getGroup().containsHumidifier(event.getHumidifier())) {
      return;
    }

    Platform.runLater(() -> hideLostError(event.getHumidifier()));
  }

  private void showLostError(Device device) {
    String message = LocaleManager.getInstance().getString("device.error.connection");
    if (device instanceof Base) {
      GroupBaseItemController baseController =
          GroupItemControllerHolder.getInstance().getBaseController(device);
      if (baseController != null) {
        baseController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    } else if (device instanceof Humidifier) {
      GroupHumItemController humController =
          GroupItemControllerHolder.getInstance().getHumController(device);
      if (humController != null) {
        humController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    }
  }

  private void hideLostError(Device device) {
    if (device instanceof Base) {
      GroupBaseItemController baseController =
          GroupItemControllerHolder.getInstance().getBaseController(device);
      if (baseController != null) {
        baseController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    } else if (device instanceof Humidifier) {
      GroupHumItemController humController =
          GroupItemControllerHolder.getInstance().getHumController(device);
      if (humController != null) {
        humController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST);
      }
    }
  }
}
