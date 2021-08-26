package com.attestorforensics.mobifumecore.controller.group.item;

import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.event.base.BaseLostEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseReconnectedEvent;
import com.attestorforensics.mobifumecore.model.event.base.BaseUpdatedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.misc.BaseLatch;
import javafx.application.Platform;

public class GroupBaseConnectionListener implements Listener {

  private final GroupBaseItemController groupBaseItemController;

  private GroupBaseConnectionListener(GroupBaseItemController groupBaseItemController) {
    this.groupBaseItemController = groupBaseItemController;
  }

  static GroupBaseConnectionListener create(GroupBaseItemController groupBaseItemController) {
    return new GroupBaseConnectionListener(groupBaseItemController);
  }

  @EventHandler
  public void onBaseLost(BaseLostEvent event) {
    if (event.getBase() != groupBaseItemController.getBase()) {
      return;
    }

    Platform.runLater(() -> {
      String message = LocaleManager.getInstance().getString("device.error.connection");
      groupBaseItemController.showError(message, true, ItemErrorType.DEVICE_CONNECTION_LOST);
    });
  }

  @EventHandler
  public void onBaseReconnected(BaseReconnectedEvent event) {
    if (event.getBase() != groupBaseItemController.getBase()) {
      return;
    }

    Platform.runLater(
        () -> groupBaseItemController.hideError(ItemErrorType.DEVICE_CONNECTION_LOST));
  }

  @EventHandler
  public void onBaseUpdated(BaseUpdatedEvent event) {
    if (event.getBase() != groupBaseItemController.getBase()) {
      return;
    }

    Base base = event.getBase();
    Platform.runLater(() -> {
      if (base.getLatch() == BaseLatch.ERROR_OTHER || base.getLatch() == BaseLatch.ERROR_NOT_REACHED
          || base.getLatch() == BaseLatch.ERROR_BLOCKED) {
        String message = LocaleManager.getInstance().getString("base.error.latch");
        groupBaseItemController.showError(message, true, ItemErrorType.BASE_LATCH);
      } else if (base.getHeaterTemperature().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.heater");
        groupBaseItemController.showError(message, true, ItemErrorType.BASE_HEATER);
      } else if (base.getTemperature().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.temperature");
        groupBaseItemController.showError(message, true, ItemErrorType.BASE_TEMPERATURE);
      } else if (base.getHumidity().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.humidity");
        groupBaseItemController.showError(message, true, ItemErrorType.BASE_HUMIDITY);
      } else {
        groupBaseItemController.hideAllError();
      }
    });
  }
}
