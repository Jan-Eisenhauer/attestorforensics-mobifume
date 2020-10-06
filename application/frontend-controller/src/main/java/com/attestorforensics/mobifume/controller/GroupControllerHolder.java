package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.model.object.Group;
import com.google.common.collect.Lists;
import java.util.List;

public class GroupControllerHolder {

  private static GroupControllerHolder instance;

  private final List<GroupController> controllers = Lists.newArrayList();

  private GroupControllerHolder() {
  }

  public static GroupControllerHolder getInstance() {
    if (instance == null) {
      instance = new GroupControllerHolder();
    }

    return instance;
  }

  public GroupController getController(Group group) {
    return controllers.stream()
        .filter(controller -> controller.getGroup() == group)
        .findFirst()
        .orElse(null);
  }

  void addController(GroupController controller) {
    controllers.add(controller);
  }

  public void removeController(Group group) {
    controllers.removeIf(controller -> controller.getGroup() == group);
  }
}
