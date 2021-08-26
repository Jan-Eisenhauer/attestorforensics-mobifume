package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.detailbox.DetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class Controller implements Initializable {

  private static final String VIEW_RESOURCE = "view/";

  private Parent root;

  public Parent getRoot() {
    checkState(root != null, "Root was not set yet");
    return root;
  }

  protected void setRoot(Parent root) {
    checkState(this.root == null, "Root can only be set once");
    this.root = root;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  protected void onLoad() {
  }

  protected void onLateLoad() {
  }

  protected void onShow() {
  }

  protected <T extends Controller> CompletableFuture<T> openView(T controller) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      completableFuture.complete(controller);
      controller.onShow();
      SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
    });

    return completableFuture;
  }

  protected <T extends Controller> CompletableFuture<T> loadAndOpenView(String viewResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + viewResource);
      controller.onLoad();
      completableFuture.complete(controller);
      controller.onLateLoad();
      controller.onShow();
      SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
    });

    return completableFuture;
  }

  protected <T extends Controller> CompletableFuture<T> loadView(String viewResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + viewResource);
      controller.onLoad();
      completableFuture.complete(controller);
      controller.onLateLoad();
    });

    return completableFuture;
  }

  protected <T extends ItemController> CompletableFuture<T> loadItem(String itemResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + "items/" + itemResource);
      controller.setParentController(this);
      controller.onLoad();
      completableFuture.complete(controller);
      controller.onLateLoad();
    });

    return completableFuture;
  }

  protected <T extends DialogController> CompletableFuture<T> loadAndOpenDialog(
      String dialogResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource("view/dialog/" + dialogResource);
      createStage(controller);
      controller.onLoad();
      completableFuture.complete(controller);
      controller.onLateLoad();
      controller.onShow();
      openDialog(controller);
    });

    return completableFuture;
  }

  protected <T extends DetailBoxController> CompletableFuture<T> loadAndShowDetailBox(
      String detailBoxResource, Node node) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource("view/detailbox/" + detailBoxResource);
      Stage stage = createStage(controller);

      Bounds bounds = node.localToScreen(node.getBoundsInLocal());
      stage.setX(bounds.getMaxX());
      stage.setY((bounds.getMaxY() + bounds.getMinY()) * 0.5D - 28);

      controller.onLoad();
      completableFuture.complete(controller);
      controller.onLateLoad();
      controller.onShow();
      showDetailBox(controller, bounds);
    });

    return completableFuture;
  }

  private <T extends Controller> T loadResource(String resource) {
    ResourceBundle resourceBundle = LocaleManager.getInstance().getResourceBundle();
    FXMLLoader loader =
        new FXMLLoader(getClass().getClassLoader().getResource(resource), resourceBundle);
    Parent resourceRoot;
    try {
      resourceRoot = loader.load();
    } catch (IOException e) {
      throw new ViewResourceException(resource, e);
    }

    T resourceController = loader.getController();
    resourceController.setRoot(resourceRoot);
    return resourceController;
  }

  private void openDialog(ChildStageController controller) {
    controller.getStage().show();
    root.getScene().getRoot().setEffect(new ColorAdjust(0, 0, -0.3, 0));
  }

  private void showDetailBox(DetailBoxController controller, Bounds bounds) {
    Stage stage = controller.getStage();
    stage.show();

    // flip stage if out of screen
    if (stage.getX() + stage.getWidth() > Screen.getPrimary().getBounds().getMaxX()) {
      controller.flip();
      stage.setX(bounds.getMinX() - stage.getWidth());
    }
  }

  private Stage createStage(ChildStageController controller) {
    Stage stage = new Stage();
    controller.setStage(stage);
    stage.initOwner(root.getScene().getWindow());
    stage.initStyle(StageStyle.TRANSPARENT);
    stage.focusedProperty().addListener((observableValue, oldFocus, newFocus) -> {
      if (newFocus != null && !newFocus) {
        controller.focusLost();
      }
    });

    Parent dialogRoot = controller.getRoot();
    Scene scene = new Scene(dialogRoot);
    scene.setFill(Color.TRANSPARENT);
    stage.setScene(scene);
    return stage;
  }
}
