package com.attestorforensics.mobifumecore.model.event.connection.wifi;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class WifiDisconnectingEvent implements Event {

  private WifiDisconnectingEvent() {
  }

  public static WifiDisconnectingEvent create() {
    return new WifiDisconnectingEvent();
  }
}
