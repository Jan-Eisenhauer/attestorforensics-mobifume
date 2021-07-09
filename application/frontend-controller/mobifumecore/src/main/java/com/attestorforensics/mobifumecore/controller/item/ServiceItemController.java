package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.controller.Controller;
import com.attestorforensics.mobifumecore.model.element.node.Device;

public abstract class ServiceItemController extends Controller {

  public abstract Device getDevice();

  public abstract void setDevice(Device device);

  public abstract void update();

  public abstract void remove();
}
