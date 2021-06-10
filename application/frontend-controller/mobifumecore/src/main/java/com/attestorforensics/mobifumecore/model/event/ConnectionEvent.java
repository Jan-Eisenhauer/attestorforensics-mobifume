package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;

public class ConnectionEvent implements Event {

  private final ConnectionStatus status;

  public ConnectionEvent(ConnectionStatus status) {
    this.status = status;
  }

  public ConnectionStatus getStatus() {
    return status;
  }

  public enum ConnectionStatus {
    WIFI_CONNECTED,
    WIFI_CONNECTING,
    WIFI_DISCONNECTED,
    WIFI_DISCONNECTING,
    BROKER_CONNECTED,
    BROKER_DISCONNECTED,
    BROKER_LOST,
    BROKER_OTHER_ONLINE
  }
}
