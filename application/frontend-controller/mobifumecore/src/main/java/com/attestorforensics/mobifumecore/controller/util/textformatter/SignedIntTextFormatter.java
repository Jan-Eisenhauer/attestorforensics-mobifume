package com.attestorforensics.mobifumecore.controller.util.textformatter;

import javafx.scene.control.TextFormatter;

public class SignedIntTextFormatter extends TextFormatter<String> {

  public SignedIntTextFormatter() {
    super(SignedIntTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String changedText = change.getText();
    if (changedText.isEmpty()) {
      return change;
    }

    String fullText = change.getControlNewText();
    if (fullText.equals("-")) {
      return change;
    }

    try {
      Integer.parseInt(fullText);
    } catch (NumberFormatException ignored) {
      change.setText("");
    }

    return change;
  }
}
