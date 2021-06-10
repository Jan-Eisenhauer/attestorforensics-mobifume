package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.util.localization.LocaleManager;
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

public class YesNoDialog {

  private final Window window;
  private final String title;
  private final String content;
  private final Consumer<Boolean> callback;
  private Stage stage;
  private YesNoController controller;

  private boolean closed = false;

  private YesNoDialog(Window window, String title, String content, Consumer<Boolean> callback) {
    this.window = window;
    this.title = title;
    this.content = content;
    this.callback = callback;
  }

  public static YesNoDialog create(Window window, String title, String content,
      Consumer<Boolean> callback) {
    return new YesNoDialog(window, title, content, callback);
  }

  public void show() {
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader =
          new FXMLLoader(getClass().getClassLoader().getResource("view/dialog/YesNoDialog.fxml"),
              resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (Boolean.FALSE.equals(newFocus) && !closed) {
          close(false);
        }
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
    closed = true;
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(accepted);
      }
    });
  }
}
