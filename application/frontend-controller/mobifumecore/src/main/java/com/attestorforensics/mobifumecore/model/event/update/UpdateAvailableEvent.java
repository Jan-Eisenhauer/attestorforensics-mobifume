package com.attestorforensics.mobifumecore.model.event.update;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class UpdateAvailableEvent implements Event {

  private final String newVersion;

  private UpdateAvailableEvent(String newVersion) {
    this.newVersion = newVersion;
  }

  public static UpdateAvailableEvent create(String newVersion) {
    return new UpdateAvailableEvent(newVersion);
  }

  public String getNewVersion() {
    return newVersion;
  }
}
