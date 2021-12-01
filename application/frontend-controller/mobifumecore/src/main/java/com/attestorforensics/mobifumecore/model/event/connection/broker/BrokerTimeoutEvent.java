package com.attestorforensics.mobifumecore.model.event.connection.broker;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class BrokerTimeoutEvent implements Event {

  private BrokerTimeoutEvent() {
  }

  public static BrokerTimeoutEvent create() {
    return new BrokerTimeoutEvent();
  }
}
