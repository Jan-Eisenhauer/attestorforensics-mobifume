package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.item.ServiceItemController;
import com.attestorforensics.mobifumecore.controller.listener.ServiceListener;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.DeviceType;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

public class ServiceController {

  @FXML
  private Parent root;

  @FXML
  private Pane devices;

  private Map<String, ServiceItemController> serviceItemController = Maps.newHashMap();

  private ServiceListener serviceListener;

  @FXML
  public void initialize() {
    serviceListener = new ServiceListener(this);
    Mobifume.getInstance().getEventDispatcher().registerListener(serviceListener);
    Mobifume.getInstance().getModelManager().getDevices().forEach(this::addDevice);
  }

  public void addDevice(Device device) {
    Platform.runLater(() -> {
      if (serviceItemController.containsKey(device.getDeviceId())) {
        serviceItemController.get(device.getDeviceId()).setDevice(device);
        return;
      }

      try {
        ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
            .getResource(
                "view/items/Service" + (device.getType() == DeviceType.BASE ? "Base" : "Hum")
                    + "Item.fxml"), resourceBundle);
        Parent root = loader.load();
        ServiceItemController controller = loader.getController();
        controller.setDevice(device);
        root.getProperties().put("controller", controller);
        serviceItemController.put(device.getDeviceId(), controller);
        devices.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Mobifume.getInstance().getEventDispatcher().unregisterListener(serviceListener);

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
  }

  @FXML
  public void onExit(ActionEvent event) {
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
    serviceItemController.get(device.getDeviceId()).update();
  }

  public void removeDevice(Device device) {
    serviceItemController.get(device.getDeviceId()).remove();
  }

  public ServiceItemController getServiceItemController(Device device) {
    return serviceItemController.get(device.getDeviceId());
  }
}
