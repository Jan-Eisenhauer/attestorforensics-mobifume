package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.detailbox.DetailBoxController;
import com.attestorforensics.mobifumecore.controller.dialog.DialogController;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

public abstract class ChildController extends Controller {

  private Stage stage;
  private ChildController childOfChildController;
  private boolean closed;

  public void focusLost() {
    if (childOfChildController == null || childOfChildController.closed) {
      childOfChildController = null;
      close();
    }
  }

  public CompletableFuture<Void> close() {
    checkState(getStage() != null, "Cannot close dialog without stage");
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

  protected void onOpen() {
  }

  @Override
  public <T extends DialogController> CompletableFuture<T> loadAndOpenDialog(
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
  public <T extends DetailBoxController> CompletableFuture<T> loadAndShowDetailBox(
      String detailBoxResource, Node parent) {
    return super.<T>loadAndShowDetailBox(detailBoxResource, parent).thenApply(controller -> {
      if (childOfChildController != null) {
        childOfChildController.close();
      }

      childOfChildController = controller;
      return controller;
    });
  }
}
