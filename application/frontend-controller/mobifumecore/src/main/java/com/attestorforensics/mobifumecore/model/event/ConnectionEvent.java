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
    WIFI_CONNECTING,
    WIFI_CONNECTED,
    WIFI_CONNECT_ERROR,
    WIFI_DISCONNECTING,
    WIFI_DISCONNECTED,
    BROKER_CONNECTING,
    BROKER_CONNECTED,
    BROKER_CONNECT_TIMEOUT,
    BROKER_CONNECTION_LOST
  }
}
