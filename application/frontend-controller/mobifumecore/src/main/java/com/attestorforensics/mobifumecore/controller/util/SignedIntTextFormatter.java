package com.attestorforensics.mobifumecore.controller.util;

import javafx.scene.control.TextFormatter;

public class SignedIntTextFormatter extends TextFormatter<String> {

  public SignedIntTextFormatter() {
    super(SignedIntTextFormatter::filter);
  }

  private static Change filter(Change change) {
    if (change.getText().isEmpty()) {
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
