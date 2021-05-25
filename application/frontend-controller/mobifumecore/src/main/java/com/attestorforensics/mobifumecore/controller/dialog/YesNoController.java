package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import lombok.Setter;

public class YesNoController {

  @Setter
  private YesNoDialog dialog;

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
  public void onYes() {
    Sound.click();
    dialog.close(true);
  }

  @FXML
  public void onNo() {
    Sound.click();
    dialog.close(false);
  }
}
