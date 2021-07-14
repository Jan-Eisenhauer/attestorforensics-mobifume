package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class SaveDiscardCancelDialogController extends DialogController {

  private Consumer<SaveDiscardCancelResult> callback;

  @FXML
  private Text title;
  @FXML
  private Text content;

  public void setCallback(Consumer<SaveDiscardCancelResult> callback) {
    this.callback = callback;
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {

  }

  @Override
  public CompletableFuture<Void> close() {
    if (callback != null) {
      callback.accept(SaveDiscardCancelResult.CANCEL);
    }

    return super.close();
  }

  @FXML
  private void onSave() {
    Sound.click();
    callback.accept(SaveDiscardCancelResult.SAVE);
    super.close();
  }

  @FXML
  private void onDiscard() {
    Sound.click();
    callback.accept(SaveDiscardCancelResult.DISCARD);
    super.close();
  }

  @FXML
  private void onCancel() {
    Sound.click();
    callback.accept(SaveDiscardCancelResult.CANCEL);
    super.close();
  }

  public enum SaveDiscardCancelResult {
    SAVE,
    DISCARD,
    CANCEL
  }
}
