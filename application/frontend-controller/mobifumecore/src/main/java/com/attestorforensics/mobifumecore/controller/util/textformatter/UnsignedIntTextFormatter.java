package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class UnsignedIntTextFormatter extends TextFormatter<String> {

  public UnsignedIntTextFormatter() {
    super(UnsignedIntTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String changedText = change.getText();
    if (changedText.isEmpty()) {
      return change;
    }

    String fullText = change.getControlNewText();

    try {
      float value = Integer.parseInt(fullText);
      if (value < 0) {
        change.setText("");
      }
    } catch (NumberFormatException ignored) {
      change.setText("");
    }

    return change;
  }
}
