package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.item.ServiceItemController;
import com.attestorforensics.mobifumecore.controller.listener.ServiceListener;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.google.common.collect.Maps;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class ServiceController extends CloseableController {

  @FXML
  private Parent root;

  @FXML
  private Pane devices;

  private final Map<String, ServiceItemController> serviceItemControllers = Maps.newHashMap();

  private ServiceListener serviceListener;

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    serviceListener = new ServiceListener(this);
    Mobifume.getInstance().getEventDispatcher().registerListener(serviceListener);
    Mobifume.getInstance().getModelManager().getDevicePool().getAllBases().forEach(this::addDevice);
    Mobifume.getInstance()
        .getModelManager()
        .getDevicePool()
        .getAllHumidifier()
        .forEach(this::addDevice);
  }

  public void addDevice(Device device) {
    Platform.runLater(() -> {
      if (serviceItemControllers.containsKey(device.getDeviceId())) {
        serviceItemControllers.get(device.getDeviceId()).setDevice(device);
        return;
      }

      this.<ServiceItemController>loadItem(
          "Service" + (device.getType() == DeviceType.BASE ? "Base" : "Hum") + "Item.fxml")
          .thenAccept(serviceItemController -> {
            Parent serviceItemRoot = serviceItemController.getRoot();
            serviceItemController.setDevice(device);
            serviceItemRoot.getProperties().put("controller", serviceItemController);
            serviceItemControllers.put(device.getDeviceId(), serviceItemController);
            devices.getChildren().add(serviceItemRoot);
          });
    });
  }

  @FXML
  public void onBack() {
    Sound.click();

    Mobifume.getInstance().getEventDispatcher().unregisterListener(serviceListener);

    close();
  }

  @FXML
  public void onExit() {
    Sound.click();

    new ConfirmDialog(root.getScene().getWindow(),
        LocaleManager.getInstance().getString("dialog.exit.title"),
        LocaleManager.getInstance().getString("dialog.exit.content"), true, accepted -> {
      if (Boolean.FALSE.equals(accepted)) {
        return;
      }

      System.exit(0);
    });
  }

  public void updateDevice(Device device) {
    serviceItemControllers.get(device.getDeviceId()).update();
  }

  public void removeDevice(Device device) {
    serviceItemControllers.get(device.getDeviceId()).remove();
  }

  public ServiceItemController getServiceItemController(Device device) {
    return serviceItemControllers.get(device.getDeviceId());
  }
}
