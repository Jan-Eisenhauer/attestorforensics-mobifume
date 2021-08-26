package com.attestorforensics.mobifumecore.controller.group.item;

import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierLostEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierUpdatedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.node.misc.HumidifierWaterState;
import javafx.application.Platform;

public class GroupHumidifierConnectionListener implements Listener {

  private final GroupHumidifierItemController groupHumidifierItemController;

  private GroupHumidifierConnectionListener(
      GroupHumidifierItemController groupHumidifierItemController) {
    this.groupHumidifierItemController = groupHumidifierItemController;
  }

  static GroupHumidifierConnectionListener create(
      GroupHumidifierItemController groupHumidifierItemController) {
    return new GroupHumidifierConnectionListener(groupHumidifierItemController);
  }

  @EventHandler
  public void onHumidifierLost(HumidifierLostEvent event) {
    if (event.getHumidifier() != groupHumidifierItemController.getHumidifier()) {
      return;
    }

    Platform.runLater(() -> {
      String message = LocaleManager.getInstance().getString("device.error.connection");
      groupHumidifierItemController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    });
  }

  @EventHandler
  public void onHumidifierReconnected(HumidifierReconnectedEvent event) {
    if (event.getHumidifier() != groupHumidifierItemController.getHumidifier()) {
      return;
    }

    Platform.runLater(
        () -> groupHumidifierItemController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST));
  }

  @EventHandler
  public void onHumidifierUpdated(HumidifierUpdatedEvent event) {
    if (event.getHumidifier() != groupHumidifierItemController.getHumidifier()) {
      return;
    }

    Humidifier humidifier = event.getHumidifier();
    Platform.runLater(() -> {
      if (humidifier.getWaterState() == HumidifierWaterState.EMPTY) {
        String message = LocaleManager.getInstance().getString("hum.error.water");
        groupHumidifierItemController.showError(message, true, ItemErrorType.HUMIDIFIER_WATER);
      } else {
        groupHumidifierItemController.hideError(ItemErrorType.HUMIDIFIER_WATER);
      }
    });
  }
}
