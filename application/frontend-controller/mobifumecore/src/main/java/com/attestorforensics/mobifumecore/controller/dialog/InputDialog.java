package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.util.i18n.LocaleManager;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class InputDialog {

  private Stage stage;
  private Window window;
  private InputController controller;
  private Consumer<String> callback;

  public InputDialog(Window window, boolean closeOnOutside, String title, String content,
      String error, InputValidator validate, Consumer<String> callback) {
    this.window = window;
    this.callback = callback;
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader =
          new FXMLLoader(getClass().getClassLoader().getResource("view/dialog/InputDialog.fxml"),
              resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      if (closeOnOutside) {
        stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
          if (Boolean.FALSE.equals(newFocus)) {
            close(null);
          }
        });
      } else {
        stage.initModality(Modality.APPLICATION_MODAL);
      }

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
          controller.setError(error);
          controller.setValidator(validate);
        }
        stage.centerOnScreen();
        stage.show();

        window.getScene().getRoot().setEffect(new ColorAdjust(0, 0, -0.3, 0));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void close(String value) {
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(value);
      }
    });
  }
}
