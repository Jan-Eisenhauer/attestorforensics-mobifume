package com.attestorforensics.mobifumecore.controller.dialog;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

public class InfoBoxController extends DialogController {

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

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {

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
