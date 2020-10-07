package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;
import com.google.common.collect.Maps;
import java.util.Map;

public class DeviceItemControllerHolder {

  private static DeviceItemControllerHolder instance;

  private final Map<Device, DeviceItemController> controllers = Maps.newHashMap();

  private DeviceItemControllerHolder() {
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
