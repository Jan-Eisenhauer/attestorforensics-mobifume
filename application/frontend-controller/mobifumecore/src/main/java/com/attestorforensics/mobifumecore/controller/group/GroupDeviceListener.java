package com.attestorforensics.mobifumecore.controller.group;

import com.attestorforensics.mobifumecore.controller.item.GroupBaseItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupHumItemController;
import com.attestorforensics.mobifumecore.controller.item.GroupItemControllerHolder;
import com.attestorforensics.mobifumecore.controller.util.ItemErrorType;
import com.attestorforensics.mobifumecore.model.element.misc.HumidifierWaterState;
import com.attestorforensics.mobifumecore.model.element.misc.Latch;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.base.BaseUpdatedEvent;
import com.attestorforensics.mobifumecore.model.event.humidifier.HumidifierUpdatedEvent;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;
import javafx.application.Platform;

public class GroupDeviceListener implements Listener {

  private final GroupController groupController;

  private GroupDeviceListener(GroupController groupController) {
    this.groupController = groupController;
  }

  static GroupDeviceListener create(GroupController groupController) {
    return new GroupDeviceListener(groupController);
  }

  @EventHandler
  public void onBaseUpdated(BaseUpdatedEvent event) {
    Base base = event.getBase();
    if (!groupController.getGroup().containsDevice(base)) {
      return;
    }

    Platform.runLater(() -> {
      GroupBaseItemController baseItemController =
          GroupItemControllerHolder.getInstance().getBaseController(base);
      if (baseItemController == null) {
        return;
      }

      if (base.getLatch() == Latch.ERROR_OTHER || base.getLatch() == Latch.ERROR_NOT_REACHED
          || base.getLatch() == Latch.ERROR_BLOCKED) {
        String message = LocaleManager.getInstance().getString("base.error.latch");
        baseItemController.showError(message, true, ItemErrorType.BASE_LATCH);
      } else if (base.getHeaterTemperature().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.heater");
        baseItemController.showError(message, true, ItemErrorType.BASE_HEATER);
      } else if (base.getTemperature().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.temperature");
        baseItemController.showError(message, true, ItemErrorType.BASE_TEMPERATURE);
      } else if (base.getHumidity().isError()) {
        String message = LocaleManager.getInstance().getString("base.error.humidity");
        baseItemController.showError(message, true, ItemErrorType.BASE_HUMIDITY);
      } else {
        baseItemController.hideAllError();
      }
    });
  }

  @EventHandler
  public void onHumidifierUpdated(HumidifierUpdatedEvent event) {
    Humidifier humidifier = event.getHumidifier();
    if (!groupController.getGroup().containsDevice(humidifier)) {
      return;
    }

    Platform.runLater(() -> {
      GroupHumItemController humidifierItemController =
          GroupItemControllerHolder.getInstance().getHumController(humidifier);
      if (humidifierItemController == null) {
        return;
      }

      if (humidifier.getWaterState() == HumidifierWaterState.EMPTY) {
        String message = LocaleManager.getInstance().getString("hum.error.water");
        humidifierItemController.showError(message, true, ItemErrorType.HUMIDIFIER_WATER);
      } else {
        humidifierItemController.hideError(ItemErrorType.HUMIDIFIER_WATER);
      }
    });
  }
}
