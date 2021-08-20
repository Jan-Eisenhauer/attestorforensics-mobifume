package com.attestorforensics.mobifumecore.model.element.node;

import com.attestorforensics.mobifumecore.model.connection.message.MessageSender;

public abstract class Device {

  private static final int OFFLINE_RSSI = -100;

  protected final MessageSender messageSender;
  protected final String deviceId;
  private int version;
  private int rssi = OFFLINE_RSSI;
  private boolean isOnline = true;

  protected Device(MessageSender messageSender, String deviceId, int version) {
    this.messageSender = messageSender;
    this.deviceId = deviceId;
    this.version = version;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getShortId() {
    String nodeNumber = deviceId.replace("node-", "");
    try {
      int parsedValue = Integer.parseInt(nodeNumber);
      return String.format("%1$06X", parsedValue);
    } catch (NumberFormatException e) {
      return nodeNumber;
    }
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

  public boolean isOnline() {
    return isOnline;
  }

  public boolean isOffline() {
    return !isOnline;
  }

  public void setOnline() {
    isOnline = true;
  }

  public void setOffline() {
    isOnline = false;
    rssi = OFFLINE_RSSI;
  }
}
