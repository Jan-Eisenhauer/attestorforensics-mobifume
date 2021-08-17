package com.attestorforensics.mobifumecore.controller.dialog;

import com.attestorforensics.mobifumecore.controller.util.Sound;
import com.attestorforensics.mobifumecore.controller.util.TabTipKeyboard;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class InputDialogController extends DialogController {

  private Consumer<InputResult> callback;
  private InputValidator validator;

  @FXML
  private Text title;
  @FXML
  private Text content;
  @FXML
  private Text error;
  @FXML
  private TextField input;
  @FXML
  private Button ok;

  public void setCallback(Consumer<InputResult> callback) {
    this.callback = callback;
  }

  public void setValidator(InputValidator validator) {
    this.validator = validator;
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  public void setError(String error) {
    this.error.setText(error);
  }

  @Override
  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    input.textProperty().addListener((observableValue, oldText, newText) -> {
      ok.disableProperty().setValue(newText == null || newText.isEmpty());
      error.setVisible(false);
    });

    input.focusedProperty().addListener((observableValue, oldState, focused) -> {
      if (focused != null && focused) {
        Platform.runLater(input::selectAll);
      }
    });

    TabTipKeyboard.onFocus(input);
  }

  @Override
  public CompletableFuture<Void> close() {
    callback.accept(InputResult.empty());
    return super.close();
  }

  @FXML
  private void onOk() {
    Sound.click();
    String inputText = input.getText();
    if (validator.isValid(inputText)) {
      callback.accept(InputResult.create(inputText));
      super.close();
    } else {
      ok.disableProperty().setValue(true);
      error.setVisible(true);
    }
  }

  @FXML
  private void onCancel() {
    Sound.click();
    callback.accept(InputResult.empty());
    super.close();
  }

  public static class InputResult {

    private final String input;

    private InputResult(String input) {
      this.input = input;
    }

    private InputResult() {
      this(null);
    }

    private static InputResult empty() {
      return new InputResult();
    }

    private static InputResult create(String input) {
      return new InputResult(input);
    }

    public Optional<String> getInput() {
      return Optional.ofNullable(input);
    }
  }
}
