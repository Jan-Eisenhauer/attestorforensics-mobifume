package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;

public interface GroupProcess {

  GroupSettings getSettings();

  void setSettings(GroupSettings settings);

  GroupStatus getStatus();

  void startSetup();

  void startHumidify();

  void startEvaporate();

  void startPurge();

  void startComplete();

  void updateHumidifying();

  long getEvaporateStartTime();

  void increaseEvaporateDuration(int duration);

  long getPurgeStartTime();

  void increasePurgeDuration(int duration);

  void sendBaseState(Base base);

  void sendHumidifierState(Humidifier humidifier);

  void stop();
}
