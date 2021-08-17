package com.attestorforensics.mobifumecore.controller;

public class ViewResourceException extends RuntimeException {

  ViewResourceException(String resource, Throwable e) {
    super("Failed loading view resource " + resource, e);
  }
}
