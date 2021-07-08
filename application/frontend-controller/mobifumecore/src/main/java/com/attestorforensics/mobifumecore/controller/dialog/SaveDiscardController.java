package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.dialog.SaveDiscardDialog.SaveDiscardAction;
import com.attestorforensics.mobifumecore.controller.util.Sound;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class SaveDiscardController {

  private SaveDiscardDialog dialog;

  @FXML
  private Text title;
  @FXML
  private Text content;

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  @FXML
  public void onSave() {
    Sound.click();
    dialog.close(SaveDiscardAction.SAVE);
  }

  @FXML
  public void onDiscard() {
    Sound.click();
    dialog.close(SaveDiscardAction.DISCARD);
  }

  @FXML
  public void onCancel() {
    Sound.click();
    dialog.close(SaveDiscardAction.CANCEL);
  }

  public void setDialog(SaveDiscardDialog dialog) {
    this.dialog = dialog;
  }
}
