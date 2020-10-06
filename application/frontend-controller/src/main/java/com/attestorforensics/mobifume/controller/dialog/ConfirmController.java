package com.attestorforensics.mobifume.controller.dialog;

import com.attestorforensics.mobifume.controller.util.Sound;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import lombok.Setter;

public class ConfirmController {

  @Setter
  private ConfirmDialog dialog;

  @FXML
  private Text title;
  @FXML
  private Text content;
  @FXML
  private Button cancel;

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  @FXML
  public void onOk() {
    Sound.play("Click");
    dialog.close(true);
  }

  @FXML
  public void onCancel() {
    Sound.play("Click");
    dialog.close(false);
  }

  void displayCancel() {
    cancel.setVisible(true);
  }
}