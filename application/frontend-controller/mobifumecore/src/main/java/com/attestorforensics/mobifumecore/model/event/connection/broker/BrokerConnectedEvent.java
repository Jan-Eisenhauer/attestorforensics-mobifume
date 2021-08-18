package com.attestorforensics.mobifumecore.model.event.connection.broker;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class BrokerConnectedEvent implements Event {

  private BrokerConnectedEvent() {
  }

  public static BrokerConnectedEvent create() {
    return new BrokerConnectedEvent();
  }
}
