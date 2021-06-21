package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.model.element.misc.Calibration;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
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

public class CalibrateDialog {

  private Stage stage;
  private Window window;
  private CalibrateController controller;
  private Consumer<Calibration> callback;

  public CalibrateDialog(Window window, Consumer<Calibration> callback,
      String calibrationName) {
    this.window = window;
    this.callback = callback;
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/dialog/CalibrateDialog.fxml"),
          resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (Boolean.TRUE.equals(newFocus)) {
          return;
        }

        close(null);
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
          controller.setCalibrationName(calibrationName);
        }
        stage.centerOnScreen();
        stage.show();

        window.getScene().getRoot().setEffect(new ColorAdjust(0, 0, -0.3, 0));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void close(Calibration calibration) {
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(calibration);
      }
    });
  }
}
