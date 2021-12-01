package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import java.util.List;
import java.util.Optional;

public interface GroupPool {

  void addGroup(Group group);

  void removeGroup(Group group);

  Optional<Group> getGroupOfBase(Base base);

  Optional<Group> getGroupOfHumidifier(Humidifier humidifier);

  List<Group> getAllGroups();
}
