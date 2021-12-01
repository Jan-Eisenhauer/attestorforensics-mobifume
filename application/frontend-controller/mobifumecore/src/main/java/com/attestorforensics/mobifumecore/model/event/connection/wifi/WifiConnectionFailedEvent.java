package com.attestorforensics.mobifumecore.model.event.connection.wifi;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class WifiConnectionFailedEvent implements Event {

  private WifiConnectionFailedEvent() {
  }

  public static WifiConnectionFailedEvent create() {
    return new WifiConnectionFailedEvent();
  }
}
