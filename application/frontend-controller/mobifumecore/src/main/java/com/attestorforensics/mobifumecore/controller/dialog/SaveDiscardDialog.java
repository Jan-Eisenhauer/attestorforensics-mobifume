package com.attestorforensics.mobifumecore.controller.dialog;

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

public class SaveDiscardDialog implements Dialog {

  private final Window window;
  private final String title;
  private final String content;
  private final Consumer<SaveDiscardAction> callback;
  private Stage stage;
  private SaveDiscardController controller;

  private boolean closed = false;

  private SaveDiscardDialog(Window window, String title, String content,
      Consumer<SaveDiscardAction> callback) {
    this.window = window;
    this.title = title;
    this.content = content;
    this.callback = callback;
  }

  public static SaveDiscardDialog create(Window window, String title, String content,
      Consumer<SaveDiscardAction> callback) {
    return new SaveDiscardDialog(window, title, content, callback);
  }

  public void show() {
    Platform.runLater(() -> {
      ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
      FXMLLoader loader = new FXMLLoader(
          getClass().getClassLoader().getResource("view/dialog/SaveDiscardDialog.fxml"),
          resourceBundle);

      stage = new Stage();
      stage.initOwner(window);
      stage.initStyle(StageStyle.TRANSPARENT);
      stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
        if (Boolean.FALSE.equals(newFocus) && !closed) {
          closeWithAction(SaveDiscardAction.CANCEL);
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

  @Override
  public void close() {
    closeWithAction(SaveDiscardAction.CANCEL);
  }

  public void closeWithAction(SaveDiscardAction action) {
    closed = true;
    Platform.runLater(() -> {
      stage.close();
      window.getScene().getRoot().setEffect(null);
      if (callback != null) {
        callback.accept(action);
      }
    });
  }

  public enum SaveDiscardAction {
    SAVE,
    DISCARD,
    CANCEL
  }
}
