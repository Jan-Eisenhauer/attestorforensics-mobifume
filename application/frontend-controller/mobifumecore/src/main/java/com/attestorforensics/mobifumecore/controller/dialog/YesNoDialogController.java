package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class YesNoDialogController extends DialogController {

  private Consumer<YesNoResult> callback;

  @FXML
  private Text title;
  @FXML
  private Text content;

  public void setCallback(Consumer<YesNoResult> callback) {
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
    callback.accept(YesNoResult.NO);
    return super.close();
  }

  @FXML
  private void onYes() {
    Sound.click();
    callback.accept(YesNoResult.YES);
    super.close();
  }

  @FXML
  private void onNo() {
    Sound.click();
    callback.accept(YesNoResult.NO);
    super.close();
  }

  public enum YesNoResult {
    YES,
    NO
  }
}
