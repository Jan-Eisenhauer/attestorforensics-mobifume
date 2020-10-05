package com.attestorforensics.mobifume.controller.dialog;

import com.attestorforensics.mobifume.controller.util.ErrorWarning;
import com.attestorforensics.mobifume.util.localization.LocaleManager;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class InfoBox {

  private Stage stage;
  private Window window;
  private Consumer<Void> callback;
  private InfoBoxController controller;

  public InfoBox(Window window, Node parent, ErrorWarning error, Consumer<Void> callback) {
    this.window = window;
    this.callback = callback;
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/dialog/InfoBox.fxml"), resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (newFocus) {
          return;
        }

        close();
      });
      try {
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        Bounds bounds = parent.localToScreen(parent.getBoundsInLocal());
        stage.setX(bounds.getMaxX());
        stage.setY((bounds.getMaxY() + bounds.getMinY()) * 0.5D - 28);

        if (loader.getController() != null) {
          controller = loader.getController();
          controller.setContent(error.getMessage());
          controller.setErrorType(error.isError());
        }
        stage.show();

        // flip stage if out of screen
        if (stage.getX() + stage.getWidth() > Screen.getPrimary().getBounds().getMaxX()) {
          controller.getFullPane().setScaleX(-1);
          controller.getContent().setScaleX(-1);
          stage.setX(bounds.getMinX() - stage.getWidth());
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void close() {
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(null);
      }
    });
  }
}
