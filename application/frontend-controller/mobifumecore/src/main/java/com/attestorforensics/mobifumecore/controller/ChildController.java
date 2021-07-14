package com.attestorforensics.mobifumecore.controller;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.CompletableFuture;
import javafx.stage.Stage;

public abstract class ChildController extends Controller {

  private Stage stage;

  public abstract CompletableFuture<Void> close();

  public void setStage(Stage stage) {
    checkState(this.stage == null, "Stage can only be set once");
    this.stage = stage;
  }

  protected Stage getStage() {
    return stage;
  }
}
