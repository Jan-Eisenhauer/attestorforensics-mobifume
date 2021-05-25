package com.attestorforensics.mobifumecore.model.event;

import com.attestorforensics.mobifumecore.model.listener.Event;
import com.attestorforensics.mobifumecore.model.object.Device;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DeviceConnectionEvent implements Event {

  private final Device device;
  private final DeviceStatus status;

  public enum DeviceStatus {
    CONNECTED,
    DISCONNECTED,
    LOST,
    STATUS_UPDATED,
    CALIBRATION_DATA_UPDATED,
    RECONNECT
  }
}
