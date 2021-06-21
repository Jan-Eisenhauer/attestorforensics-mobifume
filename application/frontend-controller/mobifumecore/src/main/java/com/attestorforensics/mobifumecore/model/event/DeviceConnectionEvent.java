package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.element.node.Device;

public class DeviceConnectionEvent implements Event {

  private final Device device;
  private final DeviceStatus status;

  public DeviceConnectionEvent(Device device, DeviceStatus status) {
    this.device = device;
    this.status = status;
  }

  public Device getDevice() {
    return device;
  }

  public DeviceStatus getStatus() {
    return status;
  }

  public enum DeviceStatus {
    CONNECTED,
    DISCONNECTED,
    LOST,
    STATUS_UPDATED,
    CALIBRATION_DATA_UPDATED,
    RECONNECT
  }
}
