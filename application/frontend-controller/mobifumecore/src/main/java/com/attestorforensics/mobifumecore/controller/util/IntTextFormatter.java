package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class IntTextFormatter extends TextFormatter<String> {

  public IntTextFormatter() {
    super(IntTextFormatter::filter);
  }

  private static Change filter(Change change) {
    if (change.getText().isEmpty()) {
      return change;
    }
    if (change.getText().equals("-")) {
      change.setText(change.getText());
      return change;
    }

    try {
      Integer.parseInt(change.getText());
      return change;
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
