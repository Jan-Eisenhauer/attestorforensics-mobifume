package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifumecore.controller.item.SupportItemController;
import com.attestorforensics.mobifumecore.controller.listener.SupportListener;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.model.object.DeviceType;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
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

public class SupportController {

  @FXML
  private Parent root;

  @FXML
  private Pane devices;

  private Map<String, SupportItemController> supportItemControllers = Maps.newHashMap();

  private SupportListener supportListener;

  @FXML
  public void initialize() {
    supportListener = new SupportListener(this);
    Mobifume.getInstance().getEventManager().registerListener(supportListener);
    Mobifume.getInstance().getModelManager().getDevices().forEach(this::addDevice);
  }

  public void addDevice(Device device) {
    Platform.runLater(() -> {
      if (supportItemControllers.containsKey(device.getId())) {
        supportItemControllers.get(device.getId()).setDevice(device);
        return;
      }

      try {
        ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader()
            .getResource(
                "view/items/Support" + (device.getType() == DeviceType.BASE ? "Base" : "Hum")
                    + "Item.fxml"), resourceBundle);
        Parent root = loader.load();
        SupportItemController controller = loader.getController();
        controller.setDevice(device);
        root.getProperties().put("controller", controller);
        supportItemControllers.put(device.getId(), controller);
        devices.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.click();

    Mobifume.getInstance().getEventManager().unregisterListener(supportListener);

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
    supportItemControllers.get(device.getId()).update();
  }

  public void removeDevice(Device device) {
    supportItemControllers.get(device.getId()).remove();
  }

  public SupportItemController getSupportItemController(Device device) {
    return supportItemControllers.get(device.getId());
  }
}
