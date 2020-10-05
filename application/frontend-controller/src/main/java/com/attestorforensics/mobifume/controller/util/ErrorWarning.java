package com.attestorforensics.mobifume.controller.util;

public class ErrorWarning {

  private String message;
  private boolean isError;

  public ErrorWarning(String message, boolean isError) {
    this.message = message;
    this.isError = isError;
  }

  public String getMessage() {
    return message;
  }

  public boolean isError() {
    return isError;
  }
}
