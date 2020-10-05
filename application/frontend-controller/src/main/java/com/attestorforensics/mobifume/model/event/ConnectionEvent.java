package com.attestorforensics.mobifume.model.event;

import com.attestorforensics.mobifume.model.listener.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConnectionEvent implements Event {

  private final ConnectionStatus status;

  public enum ConnectionStatus {
    WIFI_CONNECTED,
    WIFI_DISCONNECTED,
    BROKER_CONNECTED,
    BROKER_DISCONNECTED,
    BROKER_LOST,
    BROKER_OTHER_ONLINE
  }
}
