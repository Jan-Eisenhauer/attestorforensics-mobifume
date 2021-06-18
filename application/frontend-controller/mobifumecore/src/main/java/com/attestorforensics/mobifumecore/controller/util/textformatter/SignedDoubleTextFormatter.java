package com.attestorforensics.mobifumecore.controller.util.textformatter;

import javafx.scene.control.TextFormatter;

public class SignedDoubleTextFormatter extends TextFormatter<String> {

  public SignedDoubleTextFormatter() {
    super(SignedDoubleTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String changedText = change.getText();
    if (changedText.isEmpty()) {
      return change;
    }

    changedText = changedText.replace(",", ".");
    change.setText(changedText);

    String fullText = change.getControlNewText();
    if (fullText.equals("-") || fullText.equals(".")) {
      return change;
    }

    try {
      Float.parseFloat(fullText);
    } catch (NumberFormatException ignored) {
      change.setText("");
    }

    return change;
  }
}
