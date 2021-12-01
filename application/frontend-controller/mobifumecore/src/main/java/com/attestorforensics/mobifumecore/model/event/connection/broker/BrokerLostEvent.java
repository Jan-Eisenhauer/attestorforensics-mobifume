package com.attestorforensics.mobifumecore.model.event.connection.broker;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class BrokerLostEvent implements Event {

  private BrokerLostEvent() {
  }

  public static BrokerLostEvent create() {
    return new BrokerLostEvent();
  }
}
