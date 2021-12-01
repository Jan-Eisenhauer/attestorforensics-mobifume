package com.attestorforensics.mobifumecore.model.event.connection.wifi;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class WifiConnectedEvent implements Event {

  private WifiConnectedEvent() {
  }

  public static WifiConnectedEvent create() {
    return new WifiConnectedEvent();
  }
}
