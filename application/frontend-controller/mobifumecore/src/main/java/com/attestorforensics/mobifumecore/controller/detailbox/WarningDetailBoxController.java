package com.attestorforensics.mobifumecore.controller.detailbox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class WarningDetailBoxController extends DetailBoxController {

  @FXML
  private Text content;

  public void setWarningMessage(String warningMessage) {
    content.setText(warningMessage);
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    // nothing to initialize
  }

  @Override
  public void flip() {
    super.flip();
    content.setScaleX(-1);
  }
}
