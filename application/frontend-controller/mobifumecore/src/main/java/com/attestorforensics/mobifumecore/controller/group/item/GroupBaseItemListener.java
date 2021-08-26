package com.attestorforensics.mobifumecore.controller.group.item;

import com.attestorforensics.mobifumecore.model.event.group.GroupRemovedEvent;
import com.attestorforensics.mobifumecore.model.listener.EventHandler;
import com.attestorforensics.mobifumecore.model.listener.Listener;

public class GroupBaseItemListener implements Listener {

  private final GroupBaseItemController groupBaseItemController;

  private GroupBaseItemListener(GroupBaseItemController groupBaseItemController) {
    this.groupBaseItemController = groupBaseItemController;
  }

  static GroupBaseItemListener create(GroupBaseItemController groupBaseItemController) {
    return new GroupBaseItemListener(groupBaseItemController);
  }

  @EventHandler
  public void onGroupRemoved(GroupRemovedEvent event) {
    if(event.getGroup() != groupBaseItemController.getGroup()) {
      return;
    }

    groupBaseItemController.onRemove();
  }
}
