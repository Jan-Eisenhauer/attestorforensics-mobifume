package com.attestorforensics.mobifumecore.controller;

public class ViewResourceException extends RuntimeException {

  ViewResourceException(Class<?> type, Throwable e) {
    super("Failed loading view class " + type, e);
  }

  ViewResourceException(String resource, Throwable e) {
    super("Failed loading view resource " + resource, e);
  }
}
