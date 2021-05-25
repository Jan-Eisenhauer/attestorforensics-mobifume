package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class FloatTextFormatter extends TextFormatter<String> {

  public FloatTextFormatter() {
    super(FloatTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String text = change.getText().replace(",", ".");
    if (text.isEmpty()) {
      return change;
    }

    if (text.equals("-")) {
      change.setText(change.getText());
      return change;
    }

    if (text.equals(".")) {
      change.setText(text);
      return change;
    }

    try {
      Float.parseFloat(text);
      return change;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
