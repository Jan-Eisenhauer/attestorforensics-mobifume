package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class UnsignedFloatTextFormatter extends TextFormatter<String> {

  public UnsignedFloatTextFormatter() {
    super(UnsignedFloatTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String changedText = change.getText();
    if (changedText.isEmpty()) {
      return change;
    }

    changedText = changedText.replace(",", ".");
    change.setText(changedText);

    String fullText = change.getControlNewText();
    if (fullText.equals(".")) {
      return change;
    }

    try {
      float value = Float.parseFloat(fullText);
      if (value < 0) {
        change.setText("");
      }
    } catch (NumberFormatException ignored) {
      change.setText("");
    }

    return change;
  }
}
