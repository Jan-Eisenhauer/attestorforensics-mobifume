package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.model.object.Device;
import com.attestorforensics.mobifumecore.model.object.Filter;
import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class CreateGroupDialog {

  private Stage stage;
  private Window window;
  private CreateGroupController controller;
  private Consumer<GroupData> callback;

  private boolean lockClosing;

  public CreateGroupDialog(Window window, List<Device> devices, Consumer<GroupData> callback) {
    this.window = window;
    this.callback = callback;
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/dialog/CreateGroupDialog.fxml"),
          resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (Boolean.FALSE.equals(newFocus)) {
          close(null);
        }
      });
      try {
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        double width = stage.getOwner().getWidth() * 0.9f;
        stage.setWidth(width);

        if (loader.getController() != null) {
          controller = loader.getController();
          controller.setDialog(this);
          controller.setDevices(devices);
        }
        stage.centerOnScreen();
        stage.show();

        window.getScene().getRoot().setEffect(new ColorAdjust(0, 0, -0.3, 0));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void close(GroupData groupData) {
    if (lockClosing) {
      return;
    }
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(groupData);
      }
    });
  }

  public void removeDevice(Device device) {
    controller.removeDevice(device);
  }

  public Stage getStage() {
    return stage;
  }

  public void setLockClosing(boolean lockClosing) {
    this.lockClosing = lockClosing;
  }

  public static class GroupData {

    private final String name;
    private final List<Device> devices;
    private final List<Filter> filters;

    public GroupData(String name, List<Device> devices, List<Filter> filters) {
      this.name = name;
      this.devices = devices;
      this.filters = filters;
    }

    public String getName() {
      return name;
    }

    public List<Device> getDevices() {
      return devices;
    }

    public List<Filter> getFilters() {
      return filters;
    }
  }
}
