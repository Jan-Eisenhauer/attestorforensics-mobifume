package com.attestorforensics.mobifume.controller.dialog;

import com.attestorforensics.mobifume.controller.util.Sound;
import com.attestorforensics.mobifume.controller.util.TabTipKeyboard;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import lombok.Setter;

public class InputController {

  @Setter
  private InputDialog dialog;
  @Setter
  private InputValidator validator;

  @FXML
  private Text title;
  @FXML
  private Text content;

  @FXML
  private TextField input;
  @FXML
  private Button ok;
  @FXML
  private Text error;

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  public void setError(String error) {
    this.error.setText(error);
  }

  @FXML
  public void initialize() {
    input.textProperty().addListener((observableValue, oldText, newText) -> {
      ok.disableProperty().setValue(newText == null || newText.isEmpty());
      error.setVisible(false);
    });
    input.focusedProperty().addListener((observableValue, oldState, focused) -> {
      if (!focused) {
        return;
      }
      Platform.runLater(input::selectAll);
    });
    TabTipKeyboard.onFocus(input);
  }

  @FXML
  public void onOk() {
    Sound.click();
    if (validator.isValid(input.getText())) {
      dialog.close(input.getText());
    } else {
      ok.disableProperty().setValue(true);
      error.setVisible(true);
    }
  }

  @FXML
  public void onCancel() {
    Sound.click();
    dialog.close(null);
  }
}
