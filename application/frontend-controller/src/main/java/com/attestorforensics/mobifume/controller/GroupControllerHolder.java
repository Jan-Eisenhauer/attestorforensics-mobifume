package com.attestorforensics.mobifume.controller;

import com.attestorforensics.mobifume.model.object.Group;
import com.google.common.collect.Maps;
import java.util.Map;

public class GroupControllerHolder {

  private static GroupControllerHolder instance;

  private final Map<Group, GroupController> controllers = Maps.newHashMap();

  private GroupControllerHolder() {
  }

  public static GroupControllerHolder getInstance() {
    if (instance == null) {
      instance = new GroupControllerHolder();
    }

    return instance;
  }

  public GroupController getController(Group group) {
    return controllers.get(group);
  }

  void addController(Group group, GroupController controller) {
    controllers.put(group, controller);
  }

  public void removeController(Group group) {
    controllers.remove(group);
  }
}
