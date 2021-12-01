package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.detailbox.DetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

public abstract class ChildStageController extends Controller {

  private Stage stage;
  private ChildStageController childOfChildController;
  private boolean closed;

  public CompletableFuture<Void> close() {
    checkState(getStage() != null, "Cannot close child controller without stage");
    if (closed) {
      return CompletableFuture.completedFuture(null);
    }

    closed = true;

    if (childOfChildController != null) {
      childOfChildController.close();
    }

    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      getStage().close();
      getStage().getOwner().getScene().getRoot().setEffect(null);
      completableFuture.complete(null);
    });

    return completableFuture;
  }

  public final void setStage(Stage stage) {
    checkState(this.stage == null, "Stage can only be set once");
    this.stage = stage;
  }

  protected final Stage getStage() {
    return stage;
  }

  @Override
  protected <T extends DialogController> CompletableFuture<T> loadAndOpenDialog(
      String dialogResource) {
    return super.<T>loadAndOpenDialog(dialogResource).thenApply(controller -> {
      if (childOfChildController != null) {
        childOfChildController.close();
      }

      childOfChildController = controller;
      return controller;
    });
  }

  @Override
  protected <T extends DetailBoxController> CompletableFuture<T> loadAndShowDetailBox(
      String detailBoxResource, Node parent) {
    return super.<T>loadAndShowDetailBox(detailBoxResource, parent).thenApply(controller -> {
      if (childOfChildController != null) {
        childOfChildController.close();
      }

      childOfChildController = controller;
      return controller;
    });
  }

  void focusLost() {
    if (childOfChildController == null || childOfChildController.closed) {
      childOfChildController = null;
      close();
    }
  }
}
