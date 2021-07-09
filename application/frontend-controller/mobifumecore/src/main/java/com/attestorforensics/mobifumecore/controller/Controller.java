package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import com.attestorforensics.mobifumecore.model.i18n.LocaleManager;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;

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

  protected <T extends Controller> CompletableFuture<T> openView(T controller) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
      completableFuture.complete(controller);
    });

    return completableFuture;
  }

  protected <T extends Controller> CompletableFuture<T> loadAndOpenView(String viewResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + viewResource);
      SceneTransition.playForward(getRoot().getScene(), controller.getRoot());
      completableFuture.complete(controller);
    });

    return completableFuture;
  }

  protected <T extends Controller> CompletableFuture<T> loadView(String viewResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + viewResource);
      completableFuture.complete(controller);
    });

    return completableFuture;
  }

  protected <T extends Controller> CompletableFuture<T> loadItem(String itemResource) {
    CompletableFuture<T> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      T controller = loadResource(VIEW_RESOURCE + "items/" + itemResource);
      completableFuture.complete(controller);
    });

    return completableFuture;
  }

  //  protected <T extends Controller> void openDialog(String dialogResource) {
  //    T controller = loadResource("view/dialog/" + dialogResource);
  // 
  //    return controller;
  //  }

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
}
