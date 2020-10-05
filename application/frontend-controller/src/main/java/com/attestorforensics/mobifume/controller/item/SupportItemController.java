package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;

public interface SupportItemController {

  void setDevice(Device device);

  void update();

  void remove();
}
