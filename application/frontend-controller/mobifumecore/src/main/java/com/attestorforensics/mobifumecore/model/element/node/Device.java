package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;

public abstract class Device {

  protected final MessageSender messageSender;
  protected final String deviceId;
  private int version;
  private int rssi = -100;
  private boolean isOffline;

  protected Device(MessageSender messageSender, String deviceId, int version) {
    this.messageSender = messageSender;
    this.deviceId = deviceId;
    this.version = version;
  }

  public abstract void reset();

  public String getShortId() {
    String nodeNumber = deviceId.replace("node-", "");
    try {
      int parsedValue = Integer.parseInt(nodeNumber);
      return String.format("%1$06X", parsedValue);
    } catch (NumberFormatException e) {
      return nodeNumber;
    }
  }

  public String getDeviceId() {
    return deviceId;
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
