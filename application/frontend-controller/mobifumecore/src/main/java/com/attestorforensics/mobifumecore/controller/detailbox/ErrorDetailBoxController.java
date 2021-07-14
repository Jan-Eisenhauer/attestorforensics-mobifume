package com.attestorforensics.mobifumecore.controller.detailbox;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class ErrorDetailBoxController extends DetailBoxController {

  @FXML
  private Text content;

  public void setErrorMessage(String errorMessage) {
    content.setText(errorMessage);
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
  }

  @Override
  public void flip() {
    super.flip();
    content.setScaleX(-1);
  }
}
