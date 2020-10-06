package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.Mobifume;
import com.attestorforensics.mobifume.controller.dialog.ConfirmDialog;
import com.attestorforensics.mobifume.controller.item.SupportItemController;
import com.attestorforensics.mobifume.controller.listener.SupportListener;
import com.attestorforensics.mobifume.controller.util.SceneTransition;
import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.DeviceType;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
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

  private List<SupportItemController> supportItemControllers = Lists.newArrayList();

  private SupportListener supportListener;

  @FXML
  public void initialize() {
    supportListener = new SupportListener(this);
    Mobifume.getInstance().getEventManager().registerListener(supportListener);
    Mobifume.getInstance().getModelManager().getDevices().forEach(this::addDevice);
  }

  public void addDevice(Device device) {
    Optional<SupportItemController> optionalSupportItemController = supportItemControllers.stream()
        .filter(controller -> controller.getDevice() == device)
        .findFirst();
    if (optionalSupportItemController.isPresent()) {
      optionalSupportItemController.get().setDevice(device);
      return;
    }

    Platform.runLater(() -> {
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
        supportItemControllers.add(controller);
        devices.getChildren().add(root);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  @FXML
  public void onBack(ActionEvent event) {
    Sound.play("Click");

    Mobifume.getInstance().getEventManager().unregisterListener(supportListener);

    Node button = (Node) event.getSource();
    Scene scene = button.getScene();
    SceneTransition.playBackward(scene, root);
  }

  @FXML
  public void onExit(ActionEvent event) {
    Sound.play("Click");

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
    getSupportItemController(device).ifPresent(SupportItemController::update);
  }

  public void removeDevice(Device device) {
    getSupportItemController(device).ifPresent(SupportItemController::remove);
  }

  private Optional<SupportItemController> getSupportItemController(Device device) {
    return supportItemControllers.stream()
        .filter(controller -> controller.getDevice() == device)
        .findFirst();
  }
}
