package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;
import java.util.HashMap;
import java.util.Map;

public class DeviceItemControllerHolder {

  private static DeviceItemControllerHolder instance;

  private Map<Device, DeviceItemController> controllers;

  private DeviceItemControllerHolder() {
    controllers = new HashMap<>();
  }

  public static DeviceItemControllerHolder getInstance() {
    if (instance == null) {
      instance = new DeviceItemControllerHolder();
    }
    return instance;
  }

  public DeviceItemController getController(Device device) {
    return controllers.get(device);
  }

  void addController(Device device, DeviceItemController controller) {
    controllers.put(device, controller);
  }

  public void removeController(Device device) {
    controllers.remove(device);
  }
}
