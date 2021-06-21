package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.model.element.node.Device;

public interface ServiceItemController {

  Device getDevice();

  void setDevice(Device device);

  void update();

  void remove();
}
