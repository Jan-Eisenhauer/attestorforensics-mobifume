package com.attestorforensics.mobifumecore.controller.dialog;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.ChildController;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public abstract class DialogController extends ChildController {

  @Override
  public CompletableFuture<Void> close() {
    checkState(getStage() != null, "Cannot close dialog without stage");
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      getStage().close();
      getStage().getOwner().getScene().getRoot().setEffect(null);
    });

    return completableFuture;
  }
}
