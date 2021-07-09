package com.attestorforensics.mobifumecore.model.element.group;

public class InvalidFilterCountException extends CreateGroupException {

  InvalidFilterCountException() {
    super("Filter count does not match the base count");
  }
}
