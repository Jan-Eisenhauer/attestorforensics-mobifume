package com.attestorforensics.mobifumecore.model;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.filter.FilterFileHandler;
import com.attestorforensics.mobifumecore.model.group.GroupFactory;
import com.attestorforensics.mobifumecore.model.group.GroupPool;
import com.attestorforensics.mobifumecore.model.group.RoomFactory;
import com.attestorforensics.mobifumecore.model.group.RoomPool;
import com.attestorforensics.mobifumecore.model.node.DevicePool;
import com.attestorforensics.mobifumecore.model.filter.FilterFactory;
import com.attestorforensics.mobifumecore.model.filter.FilterPool;
import com.attestorforensics.mobifumecore.model.node.SimpleDevicePool;
import com.attestorforensics.mobifumecore.model.filter.SimpleFilterFactory;
import com.attestorforensics.mobifumecore.model.filter.SimpleFilterPool;
import com.attestorforensics.mobifumecore.model.log.LogMover;
import com.attestorforensics.mobifumecore.model.setting.GlobalSettings;
import com.attestorforensics.mobifumecore.model.setting.SettingsRepository;
import com.attestorforensics.mobifumecore.model.update.Updater;

public class MobiModelManager implements ModelManager {

  private final DevicePool devicePool;
  private final GroupPool groupPool;
  private final GroupFactory groupFactory;
  private final FilterPool filterPool;
  private final FilterFactory filterFactory;
  private GlobalSettings globalSettings;
  private final SettingsRepository settingsRepository;
  private final Updater updater;

  public MobiModelManager(GlobalSettings globalSettings, SettingsRepository settingsRepository) {
    devicePool = SimpleDevicePool.create();
    groupPool = RoomPool.create(devicePool);
    groupFactory = RoomFactory.create(this);
    filterPool = SimpleFilterPool.create();
    FilterFileHandler filterFileHandler = new FilterFileHandler();
    filterFactory = SimpleFilterFactory.create(filterFileHandler);
    filterFileHandler.loadFilters()
        .stream()
        .filter(f -> !f.isRemoved())
        .forEach(filterPool::addFilter);

    this.globalSettings = globalSettings;
    this.settingsRepository = settingsRepository;
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
  public GlobalSettings getGlobalSettings() {
    return globalSettings;
  }

  @Override
  public void setGlobalSettings(GlobalSettings globalSettings) {
    this.globalSettings = globalSettings;
    settingsRepository.save(globalSettings);
  }

  @Override
  public Updater getUpdater() {
    return updater;
  }
}
