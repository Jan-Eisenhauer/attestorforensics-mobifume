package com.attestorforensics.mobifumecore.controller.detailbox;

import static com.google.common.base.Preconditions.checkState;

import com.attestorforensics.mobifumecore.controller.ChildController;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public abstract class DetailBoxController extends ChildController {

  public void flip() {
    getRoot().setScaleX(-1);
  }

  @Override
  public CompletableFuture<Void> close() {
    checkState(getStage() != null, "Cannot close detail box without stage");
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      getStage().close();

      // TODO - check if remove of effect is necessary
      getStage().getOwner().getScene().getRoot().setEffect(null);
    });

    return completableFuture;
  }
}
