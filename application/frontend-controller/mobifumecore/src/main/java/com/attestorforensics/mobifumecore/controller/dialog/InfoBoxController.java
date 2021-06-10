package com.attestorforensics.mobifumecore.controller.dialog;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class InfoBoxController {

  @FXML
  private Pane fullPane;

  @FXML
  private Pane pane;
  @FXML
  private Polygon paneArrow;
  @FXML
  private Text content;

  public void setContent(String content) {
    this.content.setText(content);
  }

  void setErrorType(boolean errorType) {
    pane.getStyleClass().add(errorType ? "errorBox" : "warningBox");
    paneArrow.getStyleClass().add(errorType ? "errorBox" : "warningBox");
  }

  public Pane getFullPane() {
    return fullPane;
  }

  public Text getContent() {
    return content;
  }
}
