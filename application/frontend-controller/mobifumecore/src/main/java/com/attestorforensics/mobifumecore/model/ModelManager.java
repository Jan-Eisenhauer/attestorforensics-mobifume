package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.model.element.group.GroupFactory;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.FilterFactory;
import com.attestorforensics.mobifumecore.model.element.node.FilterPool;
import com.attestorforensics.mobifumecore.model.setting.Settings;
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

  /**
   * Gets the global settings.
   *
   * @return the global settings
   */
  Settings getGlobalSettings();

  Updater getUpdater();
}
