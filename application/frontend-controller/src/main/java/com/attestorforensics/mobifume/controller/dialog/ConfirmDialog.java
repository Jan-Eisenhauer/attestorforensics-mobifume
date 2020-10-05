package com.attestorforensics.mobifume.controller.dialog;

import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.io.IOException;
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

public class ConfirmDialog {

  private Stage stage;
  private Window window;
  private ConfirmController controller;
  private Consumer<Boolean> callback;

  public ConfirmDialog(Window window, String title, String content, boolean displayCancel,
      Consumer<Boolean> callback) {
    this.window = window;
    this.callback = callback;
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/dialog/ConfirmDialog.fxml"),
          resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (newFocus) {
          return;
        }

        close(false);
      });
      try {
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();

        if (loader.getController() != null) {
          controller = loader.getController();
          controller.setDialog(this);
          controller.setTitle(title);
          controller.setContent(content);
          if (displayCancel) {
            controller.displayCancel();
          }
        }
        stage.centerOnScreen();
        stage.show();

        window.getScene().getRoot().setEffect(new ColorAdjust(0, 0, -0.3, 0));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void close(boolean accepted) {
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(accepted);
      }
    });
  }
}
