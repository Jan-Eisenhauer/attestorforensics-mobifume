package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;

public class ConfirmController {

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
    Sound.click();
    dialog.close(true);
  }

  @FXML
  public void onCancel() {
    Sound.click();
    dialog.close(false);
  }

  void displayCancel() {
    cancel.setVisible(true);
  }

  public void setDialog(ConfirmDialog dialog) {
    this.dialog = dialog;
  }
}
