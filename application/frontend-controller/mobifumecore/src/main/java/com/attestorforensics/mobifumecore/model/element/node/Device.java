package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.ClientConnection;
import com.attestorforensics.mobifumecore.model.connection.MessageEncoder;

public abstract class Device {

  protected final ClientConnection clientConnection;
  private final DeviceType type;
  private final String id;
  private int version;
  private int rssi = -100;
  private boolean isOffline;

  protected Device(ClientConnection clientConnection, final DeviceType type, final String id,
      final int version) {
    this.clientConnection = clientConnection;
    this.type = type;
    this.id = id;
    this.version = version;
  }

  public abstract void reset();
  //  public void reset() {
  //    if (type == DeviceType.BASE) {
  //      getEncoder().baseReset(this);
  //    }
  //    if (type == DeviceType.HUMIDIFIER) {
  //      getEncoder().humReset(this);
  //    }
  //  }

  protected MessageEncoder getEncoder() {
    return clientConnection.getEncoder();
  }

  public String getShortId() {
    String nodeNumber = id.replace("node-", "");
    try {
      int parsedValue = Integer.parseInt(nodeNumber);
      return String.format("%1$06X", parsedValue);
    } catch (NumberFormatException e) {
      return nodeNumber;
    }
  }

  public DeviceType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getRssi() {
    return rssi;
  }

  public void setRssi(int rssi) {
    this.rssi = rssi;
  }

  public boolean isOffline() {
    return isOffline;
  }

  public void setOffline(boolean isOffline) {
    this.isOffline = isOffline;
  }
}
