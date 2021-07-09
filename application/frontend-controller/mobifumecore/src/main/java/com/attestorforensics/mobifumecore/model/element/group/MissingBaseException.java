package com.attestorforensics.mobifumecore.model.element.group;

public class MissingBaseException extends CreateGroupException {

  MissingBaseException() {
    super("No base provided");
  }
}
