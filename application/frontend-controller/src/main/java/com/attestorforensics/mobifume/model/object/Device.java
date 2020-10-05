package com.attestorforensics.mobifume.model.object;

import com.attestorforensics.mobifume.model.connection.ClientConnection;
import com.attestorforensics.mobifume.model.connection.MessageEncoder;
import lombok.Getter;
import lombok.Setter;

public abstract class Device {

  protected final ClientConnection clientConnection;
  @Getter
  private final DeviceType type;
  @Getter
  private final String id;
  @Getter
  @Setter
  private int version;
  @Getter
  @Setter
  private int rssi = -100;

  @Getter
  @Setter
  private boolean isOffline;

  public Device(ClientConnection clientConnection, final DeviceType type, final String id,
      final int version) {
    this.clientConnection = clientConnection;
    this.type = type;
    this.id = id;
    this.version = version;
  }

  public void reset() {
    if (type == DeviceType.BASE) {
      getEncoder().baseReset(this);
    }
    if (type == DeviceType.HUMIDIFIER) {
      getEncoder().humReset(this);
    }
  }

  protected MessageEncoder getEncoder() {
    return clientConnection.getEncoder();
  }
}
