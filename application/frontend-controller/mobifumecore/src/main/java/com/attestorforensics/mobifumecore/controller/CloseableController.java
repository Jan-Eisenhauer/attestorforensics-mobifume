package com.attestorforensics.mobifumecore.controller;

import com.attestorforensics.mobifumecore.controller.util.SceneTransition;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public abstract class CloseableController extends Controller {

  protected CompletableFuture<Void> close() {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      SceneTransition.playBackward(getRoot().getScene(), getRoot());
      completableFuture.complete(null);
    });

    return completableFuture;
  }
}
