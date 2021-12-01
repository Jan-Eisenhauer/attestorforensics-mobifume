package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.model.group.GroupFactory;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.filter.FilterFactory;
import com.attestorforensics.mobifumecore.model.filter.FilterPool;
import com.attestorforensics.mobifumecore.model.setting.GlobalSettings;
import com.attestorforensics.mobifumecore.model.update.Updater;

/**
 * Holds important model objects.
 */
public interface ModelManager {

  DevicePool getDevicePool();

  GroupPool getGroupPool();

  GroupFactory getGroupFactory();

  FilterPool getFilterPool();

  FilterFactory getFilterFactory();

  GlobalSettings getGlobalSettings();

  void setGlobalSettings(GlobalSettings globalSettings);

  Updater getUpdater();
}
