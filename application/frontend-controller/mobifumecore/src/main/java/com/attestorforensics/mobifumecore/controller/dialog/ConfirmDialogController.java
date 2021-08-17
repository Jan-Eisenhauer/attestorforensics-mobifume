package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class ConfirmDialogController extends DialogController {

  private Consumer<ConfirmResult> callback;

  @FXML
  private Text title;
  @FXML
  private Text content;
  @FXML
  private Button cancel;

  public void setCallback(Consumer<ConfirmResult> callback) {
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
    // nothing to initialize
  }

  @Override
  public CompletableFuture<Void> close() {
    callback.accept(ConfirmResult.CANCEL);
    return super.close();
  }

  @FXML
  private void onOk() {
    Sound.click();
    callback.accept(ConfirmResult.CONFIRM);
    super.close();
  }

  @FXML
  private void onCancel() {
    Sound.click();
    callback.accept(ConfirmResult.CANCEL);
    super.close();
  }

  public enum ConfirmResult {
    CONFIRM,
    CANCEL
  }
}
