package com.attestorforensics.mobifumecore.model.event.connection.broker;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class BrokerConnectingEvent implements Event {

  private BrokerConnectingEvent() {
  }

  public static BrokerConnectingEvent create() {
    return new BrokerConnectingEvent();
  }
}
