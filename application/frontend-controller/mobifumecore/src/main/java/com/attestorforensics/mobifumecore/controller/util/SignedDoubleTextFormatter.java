package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class SignedDoubleTextFormatter extends TextFormatter<String> {

  public SignedDoubleTextFormatter() {
    super(SignedDoubleTextFormatter::filter);
  }

  private static Change filter(Change change) {
    String text = change.getText().replace(",", ".");
    if (text.isEmpty()) {
      return change;
    }
    if (text.equals(".")) {
      change.setText(text);
      return change;
    }

    try {
      Double.parseDouble(text);
      return change;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
