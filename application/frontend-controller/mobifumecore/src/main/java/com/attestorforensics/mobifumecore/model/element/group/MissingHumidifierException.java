package com.attestorforensics.mobifumecore.model.element.group;

public class MissingHumidifierException extends CreateGroupException {

  MissingHumidifierException() {
    super("No humidifier provided");
  }
}
