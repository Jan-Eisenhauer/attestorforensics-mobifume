package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.FilterFileHandler;
import com.attestorforensics.mobifumecore.model.element.group.GroupFactory;
import com.attestorforensics.mobifumecore.model.element.group.GroupPool;
import com.attestorforensics.mobifumecore.model.element.group.RoomFactory;
import com.attestorforensics.mobifumecore.model.element.group.RoomPool;
import com.attestorforensics.mobifumecore.model.element.node.DevicePool;
import com.attestorforensics.mobifumecore.model.element.node.FilterFactory;
import com.attestorforensics.mobifumecore.model.element.node.FilterPool;
import com.attestorforensics.mobifumecore.model.element.node.SimpleDevicePool;
import com.attestorforensics.mobifumecore.model.element.node.SimpleFilterFactory;
import com.attestorforensics.mobifumecore.model.element.node.SimpleFilterPool;
import com.attestorforensics.mobifumecore.model.log.LogMover;
import com.attestorforensics.mobifumecore.model.setting.Settings;
import com.attestorforensics.mobifumecore.model.update.Updater;

public class MobiModelManager implements ModelManager {

  private final DevicePool devicePool;
  private final GroupPool groupPool;
  private final GroupFactory groupFactory;
  private final FilterPool filterPool;
  private final FilterFactory filterFactory;
  private final Settings globalSettings;
  private final Updater updater;

  public MobiModelManager(Settings globalSettings) {
    devicePool = SimpleDevicePool.create();
    groupPool = RoomPool.create(devicePool);
    groupFactory = RoomFactory.create(globalSettings);
    filterPool = SimpleFilterPool.create();
    FilterFileHandler filterFileHandler = new FilterFileHandler();
    filterFactory = SimpleFilterFactory.create(filterFileHandler);
    filterFileHandler.loadFilters()
        .stream()
        .filter(f -> !f.isRemoved())
        .forEach(filterPool::addFilter);

    this.globalSettings = globalSettings;
    updater = Updater.create(Mobifume.getInstance().getScheduledExecutorService(),
        Mobifume.getInstance().getEventDispatcher());
    updater.startCheckingForUpdate();

    LogMover logMover =
        LogMover.create(Mobifume.getInstance().getScheduledExecutorService(), updater);
    logMover.startMovingToUsb();
  }

  @Override
  public DevicePool getDevicePool() {
    return devicePool;
  }

  @Override
  public GroupPool getGroupPool() {
    return groupPool;
  }

  @Override
  public GroupFactory getGroupFactory() {
    return groupFactory;
  }

  @Override
  public FilterPool getFilterPool() {
    return filterPool;
  }

  @Override
  public FilterFactory getFilterFactory() {
    return filterFactory;
  }

  @Override
  public Settings getGlobalSettings() {
    return globalSettings;
  }

  @Override
  public Updater getUpdater() {
    return updater;
  }
}
