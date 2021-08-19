package com.attestorforensics.mobifumecore.controller.service;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.CloseableController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialogController.ConfirmResult;
import com.attestorforensics.mobifumecore.controller.service.item.ServiceBaseItemController;
import com.attestorforensics.mobifumecore.controller.service.item.ServiceItemController;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.google.common.collect.Maps;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class ServiceController extends CloseableController {

  @FXML
  private Pane devices;

  private final Map<String, ServiceItemController> serviceItemControllers = Maps.newHashMap();

  private ServiceDeviceListener serviceListener;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    serviceListener = ServiceDeviceListener.create(this);
    Mobifume.getInstance().getEventDispatcher().registerListener(serviceListener);
    Mobifume.getInstance().getModelManager().getDevicePool().getAllBases().forEach(this::addDevice);
    Mobifume.getInstance()
        .getModelManager()
        .getDevicePool()
        .getAllHumidifier()
        .forEach(this::addDevice);
  }

  @Override
  protected CompletableFuture<Void> close() {
    Mobifume.getInstance().getEventDispatcher().unregisterListener(serviceListener);
    return super.close();
  }

  @FXML
  private void onBack() {
    Sound.click();
    close();
  }

  @FXML
  public void onExit() {
    Sound.click();

    this.<ConfirmDialogController>loadAndOpenDialog("ConfirmDialog.fxml").thenAccept(controller -> {
      controller.setCallback(confirmResult -> {
        if (confirmResult == ConfirmResult.CONFIRM) {
          System.exit(0);
        }
      });

      controller.setTitle(LocaleManager.getInstance().getString("dialog.exit.title"));
      controller.setContent(LocaleManager.getInstance().getString("dialog.exit.content"));
    });
  }

  void addDevice(Device device) {
    Platform.runLater(() -> {
      if (serviceItemControllers.containsKey(device.getDeviceId())) {
        serviceItemControllers.get(device.getDeviceId()).setDevice(device);
        return;
      }

      this.<ServiceItemController>loadItem(
              "Service" + (device instanceof Base ? "Base" : "Hum") + "Item.fxml")
          .thenAccept(serviceItemController -> {
            Parent serviceItemRoot = serviceItemController.getRoot();
            serviceItemController.setDevice(device);
            serviceItemRoot.getProperties().put("controller", serviceItemController);
            serviceItemControllers.put(device.getDeviceId(), serviceItemController);
            devices.getChildren().add(serviceItemRoot);
          });
    });
  }

  void removeDevice(Device device) {
    Platform.runLater(() -> serviceItemControllers.get(device.getDeviceId()).remove());
  }

  void updateDevice(Device device) {
    Platform.runLater(() -> serviceItemControllers.get(device.getDeviceId()).update());
  }

  void updateCalibration(Device device) {
    Platform.runLater(() -> ((ServiceBaseItemController) serviceItemControllers.get(
        device.getDeviceId())).updateCalibration());
  }
}
