package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.Group;
import com.google.common.collect.Lists;
import java.util.List;

public class GroupItemControllerHolder {

  private static GroupItemControllerHolder instance;

  private List<GroupBaseItemController> baseControllers = Lists.newArrayList();
  private List<GroupHumItemController> humControllers = Lists.newArrayList();

  private GroupItemControllerHolder() {
  }

  public static GroupItemControllerHolder getInstance() {
    if (instance == null) {
      instance = new GroupItemControllerHolder();
    }

    return instance;
  }

  public GroupBaseItemController getBaseController(Device base) {
    return baseControllers.stream()
        .filter(controller -> controller.getBase() == base)
        .findFirst()
        .orElse(null);
  }

  void addBaseController(GroupBaseItemController controller) {
    baseControllers.add(controller);
  }

  public GroupHumItemController getHumController(Device hum) {
    return humControllers.stream()
        .filter(controller -> controller.getHumidifier() == hum)
        .findFirst()
        .orElse(null);
  }

  void addHumController(GroupHumItemController controller) {
    humControllers.add(controller);
  }

  public void removeGroupItems(Group group) {
    group.getBases().forEach(this::removeBaseController);
    group.getHumidifiers().forEach(this::removeHumController);
  }

  private void removeBaseController(Device base) {
    baseControllers.removeIf(controller -> controller.getBase() == base);
  }

  private void removeHumController(Device hum) {
    humControllers.removeIf(controller -> controller.getHumidifier() == hum);
  }
}
