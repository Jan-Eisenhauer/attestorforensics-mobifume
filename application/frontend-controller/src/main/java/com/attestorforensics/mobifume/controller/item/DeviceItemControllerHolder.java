package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;
import com.google.common.collect.Lists;
import java.util.List;

public class DeviceItemControllerHolder {

  private static DeviceItemControllerHolder instance;

  private final List<DeviceItemController> controllers = Lists.newArrayList();

  private DeviceItemControllerHolder() {
  }

  public static DeviceItemControllerHolder getInstance() {
    if (instance == null) {
      instance = new DeviceItemControllerHolder();
    }

    return instance;
  }

  public DeviceItemController getController(Device device) {
    return controllers.stream()
        .filter(controller -> controller.getDevice() == device)
        .findFirst()
        .orElse(null);
  }

  void addController(DeviceItemController controller) {
    controllers.add(controller);
  }

  public void removeController(Device device) {
    controllers.removeIf(controller -> controller.getDevice() == device);
  }
}
