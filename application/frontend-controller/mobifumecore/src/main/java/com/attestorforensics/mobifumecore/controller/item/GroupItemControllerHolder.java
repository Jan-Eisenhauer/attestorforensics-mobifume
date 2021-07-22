package com.attestorforensics.mobifumecore.controller.item;

import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.group.Group;
import com.google.common.collect.Maps;
import java.util.Map;

public class GroupItemControllerHolder {

  private static GroupItemControllerHolder instance;

  private final Map<Device, GroupBaseItemController> baseControllers = Maps.newHashMap();
  private final Map<Device, GroupHumItemController> humControllers = Maps.newHashMap();

  private GroupItemControllerHolder() {
  }

  public static GroupItemControllerHolder getInstance() {
    if (instance == null) {
      instance = new GroupItemControllerHolder();
    }

    return instance;
  }

  public GroupBaseItemController getBaseController(Device base) {
    return baseControllers.get(base);
  }

  void addBaseController(Device base, GroupBaseItemController controller) {
    baseControllers.put(base, controller);
  }

  public GroupHumItemController getHumController(Device hum) {
    return humControllers.get(hum);
  }

  public void addHumController(Device hum, GroupHumItemController controller) {
    humControllers.put(hum, controller);
  }

  public void removeGroupItems(Group group) {
    group.getBases().forEach(this::removeBaseController);
    group.getHumidifiers().forEach(this::removeHumController);
  }

  private void removeBaseController(Device base) {
    baseControllers.remove(base);
  }

  private void removeHumController(Device hum) {
    humControllers.remove(hum);
  }
}
