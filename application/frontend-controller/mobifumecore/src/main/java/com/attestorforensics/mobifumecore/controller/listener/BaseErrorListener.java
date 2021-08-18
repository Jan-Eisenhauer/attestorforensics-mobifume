package com.attestorforensics.mobifumecore.controller.listener;

import com.attestorforensics.mobifumecore.model.listener.Listener;

public class BaseErrorListener implements Listener {

  // TODO - implement listener

  //  private final Map<Device, Set<BaseErrorEvent.ErrorType>> errors = Maps.newHashMap();
  //
  //  @EventHandler
  //  public void onBaseError(BaseErrorEvent event) {
  //    Set<ErrorType> errorTypes =
  //        errors.computeIfAbsent(event.getBase(), device -> Sets.newHashSet());
  //    errorTypes.add(event.getError());
  //    updateErrors(event.getBase());
  //  }
  //
  //  @EventHandler
  //  public void onBaseErrorResolved(BaseErrorResolvedEvent event) {
  //    Set<BaseErrorEvent.ErrorType> deviceErrors = errors.get(event.getBase());
  //    if (deviceErrors == null) {
  //      return;
  //    }
  //
  //    deviceErrors.remove(event.getResolved());
  //    if (deviceErrors.isEmpty()) {
  //      errors.remove(event.getBase());
  //    }
  //
  //    updateErrors(event.getBase());
  //  }
  //
  //  void updateErrors(Device base) {
  //    Platform.runLater(() -> {
  //      if (isDisplayed(base, BaseErrorEvent.ErrorType.LATCH)) {
  //        String message = LocaleManager.getInstance().getString("base.error.latch");
  //        showDeviceItemError(base, message, ItemErrorType.BASE_LATCH);
  //        showGroupBaseItemError(base, message, ItemErrorType.BASE_LATCH);
  //      } else if (isDisplayed(base, BaseErrorEvent.ErrorType.HEATER)) {
  //        String message = LocaleManager.getInstance().getString("base.error.heater");
  //        showDeviceItemError(base, message, ItemErrorType.BASE_HEATER);
  //        showGroupBaseItemError(base, message, ItemErrorType.BASE_HEATER);
  //      } else if (isDisplayed(base, BaseErrorEvent.ErrorType.TEMPERATURE)) {
  //        String message = LocaleManager.getInstance().getString("base.error.temperature");
  //        showDeviceItemError(base, message, ItemErrorType.BASE_TEMPERATURE);
  //        showGroupBaseItemError(base, message, ItemErrorType.BASE_TEMPERATURE);
  //      } else if (isDisplayed(base, BaseErrorEvent.ErrorType.HUMIDITY)) {
  //        String message = LocaleManager.getInstance().getString("base.error.humidity");
  //        showDeviceItemError(base, message, ItemErrorType.BASE_HUMIDITY);
  //        showGroupBaseItemError(base, message, ItemErrorType.BASE_HUMIDITY);
  //      } else {
  //        hideDeviceItemError(base);
  //        hideGroupBaseItemError(base);
  //      }
  //    });
  //  }
  //
  //  private boolean isDisplayed(Device device, BaseErrorEvent.ErrorType errorType) {
  //    if (!errors.containsKey(device)) {
  //      return false;
  //    }
  //    return errors.get(device).contains(errorType);
  //  }
  //
  //  private void showDeviceItemError(Device base, String message, ItemErrorType errorType) {
  //    DeviceItemController deviceController =
  //        DeviceItemControllerHolder.getInstance().getController(base);
  //    deviceController.showError(message, true, errorType);
  //  }
  //
  //  private void showGroupBaseItemError(Device base, String message, ItemErrorType errorType) {
  //    GroupBaseItemController baseController =
  //        GroupItemControllerHolder.getInstance().getBaseController(base);
  //    if (baseController == null) {
  //      return;
  //    }
  //    baseController.showError(message, true, errorType);
  //  }
  //
  //  private void hideDeviceItemError(Device base) {
  //    DeviceItemController deviceController =
  //        DeviceItemControllerHolder.getInstance().getController(base);
  //    deviceController.hideAllError();
  //  }
  //
  //  private void hideGroupBaseItemError(Device base) {
  //    GroupBaseItemController baseController =
  //        GroupItemControllerHolder.getInstance().getBaseController(base);
  //    if (baseController == null) {
  //      return;
  //    }
  //    baseController.hideAllError();
  //  }
}
