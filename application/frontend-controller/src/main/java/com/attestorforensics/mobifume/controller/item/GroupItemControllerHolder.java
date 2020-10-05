package com.attestorforensics.mobifume.controller.item;

import com.attestorforensics.mobifume.model.object.Device;
import com.attestorforensics.mobifume.model.object.Group;
import java.util.HashMap;
import java.util.Map;

public class GroupItemControllerHolder {

  private static GroupItemControllerHolder instance;

  private Map<Device, GroupBaseItemController> baseController;
  private Map<Device, GroupHumItemController> humController;

  private GroupItemControllerHolder() {
    baseController = new HashMap<>();
    humController = new HashMap<>();
  }

  public static GroupItemControllerHolder getInstance() {
    if (instance == null) {
      instance = new GroupItemControllerHolder();
    }
    return instance;
  }

  public GroupBaseItemController getBaseController(Device base) {
    return baseController.get(base);
  }

  void addBaseController(Device base, GroupBaseItemController controller) {
    baseController.put(base, controller);
  }

  public GroupHumItemController getHumController(Device hum) {
    return humController.get(hum);
  }

  void addHumController(Device hum, GroupHumItemController controller) {
    humController.put(hum, controller);
  }

  public void removeGroupItems(Group group) {
    group.getBases().forEach(this::removeBaseController);
    group.getHumidifiers().forEach(this::removeHumController);
  }

  private void removeBaseController(Device base) {
    baseController.remove(base);
  }

  private void removeHumController(Device hum) {
    humController.remove(hum);
  }
}
