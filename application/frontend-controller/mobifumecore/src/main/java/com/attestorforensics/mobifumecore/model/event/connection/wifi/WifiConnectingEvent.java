package com.attestorforensics.mobifumecore.model.event.connection.wifi;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class WifiConnectingEvent implements Event {

  private WifiConnectingEvent() {
  }

  public static WifiConnectingEvent create() {
    return new WifiConnectingEvent();
  }
}
