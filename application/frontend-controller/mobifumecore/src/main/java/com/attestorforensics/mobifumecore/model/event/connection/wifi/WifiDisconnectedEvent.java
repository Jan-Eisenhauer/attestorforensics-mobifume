package com.attestorforensics.mobifumecore.model.event.connection.wifi;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class WifiDisconnectedEvent implements Event {

  private WifiDisconnectedEvent() {
  }

  public static WifiDisconnectedEvent create() {
    return new WifiDisconnectedEvent();
  }
}
