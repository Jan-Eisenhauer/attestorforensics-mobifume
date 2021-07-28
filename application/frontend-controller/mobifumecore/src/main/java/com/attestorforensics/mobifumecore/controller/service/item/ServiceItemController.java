package com.attestorforensics.mobifumecore.controller.service.item;

import com.attestorforensics.mobifumecore.controller.ItemController;
import com.attestorforensics.mobifumecore.model.element.node.Device;

public abstract class ServiceItemController extends ItemController {

  public abstract Device getDevice();

  public abstract void setDevice(Device device);

  public abstract void update();

  public abstract void remove();
}
