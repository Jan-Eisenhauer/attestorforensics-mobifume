package com.attestorforensics.mobifumecore.model.element.group;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.attestorforensics.mobifumecore.Mobifume;
import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.event.group.GroupCanceledEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.evaporate.EvaporateStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.humidify.HumidifyStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyDisabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.HumidifyEnabledEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeFinishedEvent;
import com.attestorforensics.mobifumecore.model.event.group.purge.PurgeStartedEvent;
import com.attestorforensics.mobifumecore.model.event.group.setup.SetupStartedEvent;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.EvaporantSettings;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class Room implements Group {

  private static final int HUMIDIFY_CIRCULATE_DURATION = 30;

  private final Logger logger;
  private final String name;
  private final int cycleNumber;
  private final List<Base> bases;
  private final List<Humidifier> humidifiers;
  private final List<Filter> filters;
  private GroupSettings settings;

  private GroupStatus status = GroupStatus.START;
  private boolean humidifySetpointReached;
  private int humidifyMaxTimes;
  private boolean humidifying;
  private ScheduledFuture<?> updateLatchTask;
  private long evaporateStartTime;
  private ScheduledFuture<?> evaporateTask;
  private ScheduledFuture<?> evaporateTimeTask;
  private long purgeStartTime;
  private ScheduledFuture<?> purgeTask;

  private Room(RoomBuilder roomBuilder) {
    logger = CustomLogger.createGroupLogger(this);
    this.name = roomBuilder.name;
    this.cycleNumber = roomBuilder.cycleNumber;
    this.bases = roomBuilder.bases;
    this.humidifiers = roomBuilder.humidifiers;
    this.filters = roomBuilder.filters;
    this.settings = roomBuilder.settings;

    if (bases.isEmpty()) {
      throw new IllegalArgumentException("No bases provided!");
    }

    if (humidifiers.isEmpty()) {
      throw new IllegalArgumentException("No humidifiers provided!");
    }

    if (bases.size() != filters.size()) {
      throw new IllegalArgumentException(
          "The count of filters is not the same as the count of bases!");
    }
  }

  public static RoomBuilder builder() {
    return new RoomBuilder();
  }

  @Override
  public Logger getLogger() {
    return logger;
  }

  @Override
  public boolean containsBase(Base base) {
    return bases.contains(base);
  }

  @Override
  public boolean containsHumidifier(Humidifier humidifier) {
    return humidifiers.contains(humidifier);
  }

  @Override
  public List<Base> getBases() {
    return Collections.unmodifiableList(bases);
  }

  @Override
  public List<Humidifier> getHumidifiers() {
    return Collections.unmodifiableList(humidifiers);
  }

  @Override
  public void setSettings(GroupSettings settings) {
    this.settings = settings;
    if (Objects.nonNull(evaporateTask) && !evaporateTask.isDone()) {
      createOrUpdateEvaporateTask();
    }

    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      createOrUpdatePurgeTask();
    }
  }

  @Override
  public DoubleSensor getTemperature() {
    OptionalDouble average = bases.stream()
        .filter(base -> base.getTemperature().isValid() && base.isOnline())
        .mapToDouble(base -> base.getTemperature().value())
        .average();
    if (average.isPresent()) {
      return DoubleSensor.of(average.getAsDouble());
    } else {
      return DoubleSensor.error();
    }
  }

  @Override
  public DoubleSensor getHumidity() {
    OptionalDouble average = bases.stream()
        .filter(base -> base.getHumidity().isValid() && base.isOnline())
        .mapToDouble(base -> base.getHumidity().value())
        .average();
    if (average.isPresent()) {
      return DoubleSensor.of(average.getAsDouble());
    } else {
      return DoubleSensor.error();
    }
  }

  @Override
  public void setupStart() {
    status = GroupStatus.START;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    bases.forEach(Base::sendReset);
    humidifiers.forEach(Humidifier::sendReset);
    Mobifume.getInstance().getEventDispatcher().call(SetupStartedEvent.create(this));
  }

  @Override
  public void startHumidify() {
    if (status != GroupStatus.START) {
      return;
    }

    status = GroupStatus.HUMIDIFY;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);

    getBases().forEach(base -> {
      base.sendTime(HUMIDIFY_CIRCULATE_DURATION);
      base.sendHeaterSetpoint(0);
      base.sendLatchCirculate();
    });

    // send every 5 min that bases latch should circulate
    updateLatchTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .scheduleAtFixedRate(this::updateHumidifyingLatch, 5, 5, TimeUnit.MINUTES);

    humidifying = false;
    enableHumidifying();
    Mobifume.getInstance().getEventDispatcher().call(HumidifyStartedEvent.create(this));
  }

  private void updateHumidifyingLatch() {
    if (status != GroupStatus.HUMIDIFY) {
      updateLatchTask.cancel(false);
      return;
    }

    getBases().forEach(base -> base.sendTime(HUMIDIFY_CIRCULATE_DURATION));
  }

  private void enableHumidifying() {
    if (humidifying) {
      return;
    }

    humidifying = true;
    CustomLogger.info(this, "SET_HUMIDIFY", true);
    CustomLogger.logGroupSettings(this);
    getHumidifiers().forEach(Humidifier::sendHumidifyEnable);
  }

  private void disableHumidifying() {
    if (!humidifying) {
      return;
    }

    humidifying = false;
    CustomLogger.info(this, "SET_HUMIDIFY", false);
    CustomLogger.logGroupSettings(this);
    getHumidifiers().forEach(Humidifier::sendHumidifyDisable);
  }

  @Override
  public void startEvaporate() {
    status = GroupStatus.EVAPORATE;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    EvaporantSettings evaporantSettings = settings.evaporantSettings();
    double evaporantAmount = evaporantSettings.roomWidth() * evaporantSettings.roomDepth()
        * evaporantSettings.roomHeight() * evaporantSettings.evaporantAmountPerCm();
    filters.forEach(
        filter -> filter.addRun(cycleNumber, evaporantSettings.evaporant(), evaporantAmount,
            filters.size()));

    humidifySetpointReached = true;

    cancelEvaporateTaskIfScheduled();
    evaporateStartTime = System.currentTimeMillis();

    getBases().forEach(base -> base.sendTime(settings.evaporateSettings().evaporateTime()));
    updateHeaterSetpoint();
    getBases().forEach(Base::sendLatchCirculate);

    createOrUpdateEvaporateTask();

    Mobifume.getInstance().getEventDispatcher().call(EvaporateStartedEvent.create(this));
  }

  private void cancelEvaporateTaskIfScheduled() {
    if (Objects.nonNull(evaporateTask) && !evaporateTask.isDone()) {
      evaporateTask.cancel(false);
    }

    if (evaporateTimeTask != null) {
      evaporateTimeTask.cancel(false);
    }
  }

  private void createOrUpdateEvaporateTask() {
    cancelEvaporateTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - evaporateStartTime;
    long timeLeft = settings.evaporateSettings().evaporateTime() * 60 * 1000L - timePassed;
    evaporateTask = Mobifume.getInstance().getScheduledExecutorService().schedule(() -> {
      Mobifume.getInstance().getEventDispatcher().call(EvaporateFinishedEvent.create(this));
      startPurge();
      evaporateTimeTask.cancel(false);
    }, timeLeft, TimeUnit.MILLISECONDS);
    evaporateTimeTask =
        Mobifume.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() -> {
          if (status != GroupStatus.EVAPORATE) {
            return;
          }

          long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
          int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
          getBases().forEach(base -> base.sendTime(
              settings.evaporateSettings().evaporateTime() - passedTimeInMinutes));
        }, 60L, 60L, TimeUnit.MINUTES);
  }

  @Override
  public void startPurge() {
    status = GroupStatus.PURGE;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);

    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    purgeStartTime = System.currentTimeMillis();

    disableHumidifying();
    getBases().forEach(base -> {
      base.sendHeaterSetpoint(0);
      base.sendLatchPurge();
    });

    createOrUpdatePurgeTask();

    Mobifume.getInstance().getEventDispatcher().call(PurgeStartedEvent.create(this));
  }

  private void cancelPurgeTaskIfScheduled() {
    if (Objects.nonNull(purgeTask) && !purgeTask.isDone()) {
      purgeTask.cancel(false);
    }
  }

  private void createOrUpdatePurgeTask() {
    cancelPurgeTaskIfScheduled();
    long timePassed = System.currentTimeMillis() - purgeStartTime;
    long timeLeft = settings.purgeSettings().purgeTime() * 60 * 1000L - timePassed;
    purgeTask = Mobifume.getInstance()
        .getScheduledExecutorService()
        .schedule(this::finish, timeLeft, TimeUnit.MILLISECONDS);
  }

  public void updateHumidify() {
    checkHumidify();
  }

  @Override
  public void cancel() {
    CustomLogger.info(this, "CANCEL");
    switch (status) {
      case HUMIDIFY:
        setupStart();
        break;
      case EVAPORATE:
        startPurge();
        break;
      case PURGE:
        finish();
        break;
      default:
        bases.forEach(Base::sendReset);
        humidifiers.forEach(Humidifier::sendReset);
        status = GroupStatus.CANCEL;
        CustomLogger.logGroupState(this);
        Mobifume.getInstance().getEventDispatcher().call(GroupCanceledEvent.create(this));
        break;
    }
  }

  @Override
  public void updateHeaterSetpoint() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int heaterTemperature = settings.evaporateSettings().heaterTemperature();
    CustomLogger.info(this, "UPDATE_HEATERSETPOINT", heaterTemperature);
    CustomLogger.logGroupSettings(this);
    getBases().forEach(base -> base.sendHeaterSetpoint(heaterTemperature));
  }

  @Override
  public void sendState(Device device) {
    CustomLogger.info(this, "SENDSTATE", device.getDeviceId(), device.getClass().getSimpleName());
    if (device instanceof Base) {
      sendBaseState((Base) device);
    } else if (device instanceof Humidifier) {
      sendHumidifierState((Humidifier) device);
    }
  }

  @Override
  public void updateHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    int evaporateTime = settings.evaporateSettings().evaporateTime();
    CustomLogger.info(this, "UPDATE_HEATTIMER", evaporateStartTime, evaporateTime);
    CustomLogger.logGroupSettings(this);
    long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
    int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
    getBases().forEach(base -> base.sendTime(evaporateTime - passedTimeInMinutes));
    createOrUpdateEvaporateTask();
  }

  @Override
  public void resetHeatTimer() {
    if (status != GroupStatus.EVAPORATE) {
      return;
    }

    evaporateStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_HEATTIMER", evaporateStartTime,
        settings.evaporateSettings().evaporateTime());
    CustomLogger.logGroupSettings(this);
    updateHeatTimer();
  }

  @Override
  public void updatePurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    CustomLogger.info(this, "UPDATE_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(this);
    createOrUpdatePurgeTask();
  }

  @Override
  public void resetPurgeTimer() {
    if (status != GroupStatus.PURGE) {
      return;
    }

    purgeStartTime = System.currentTimeMillis();
    CustomLogger.info(this, "RESET_PURGETIMER", purgeStartTime,
        settings.purgeSettings().purgeTime());
    CustomLogger.logGroupSettings(this);
    updatePurgeTimer();
  }

  private void checkHumidify() {
    if (status != GroupStatus.HUMIDIFY && status != GroupStatus.EVAPORATE) {
      return;
    }

    int humiditySetpoint = settings.humidifySettings().humiditySetpoint();
    if (!isHumidifySetpointReached()) {
      if (getHumidity().isValid() && getHumidity().value() >= humiditySetpoint) {
        humidifyMaxTimes++;
        if (humidifyMaxTimes >= 5) {
          humidifySetpointReached = true;
          Mobifume.getInstance().getEventDispatcher().call(HumidifyFinishedEvent.create(this));
        }
      }
      return;
    }

    if (isHumidifying() && getHumidity().isValid()
        && getHumidity().value() >= humiditySetpoint + settings.humidifySettings()
        .humidityPuffer()) {
      disableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyEnabledEvent.create(this));
    }
    if (!isHumidifying() && getHumidity().isValid() && getHumidity().value() <= humiditySetpoint) {
      enableHumidifying();
      Mobifume.getInstance().getEventDispatcher().call(HumidifyDisabledEvent.create(this));
    }
  }

  private void sendBaseState(Base base) {
    if (status == GroupStatus.EVAPORATE) {
      long alreadyPassedTime = System.currentTimeMillis() - evaporateStartTime;
      int passedTimeInMinutes = (int) (alreadyPassedTime / (1000 * 60f));
      base.sendTime(settings.evaporateSettings().evaporateTime() - passedTimeInMinutes);
      base.forceSendHeaterSetpoint(settings.evaporateSettings().heaterTemperature());
    } else {
      base.forceSendHeaterSetpoint(0);
    }

    if (status == GroupStatus.HUMIDIFY) {
      base.sendTime(HUMIDIFY_CIRCULATE_DURATION);
    }

    if (status == GroupStatus.HUMIDIFY || status == GroupStatus.EVAPORATE) {
      base.forceSendLatchCirculate();
    } else {
      base.forceSendLatchPurge();
    }
  }

  private void sendHumidifierState(Humidifier humidifier) {
    if (humidifying) {
      humidifier.sendHumidifyEnable();
    } else {
      humidifier.sendHumidifyDisable();
    }
  }

  private void finish() {
    status = GroupStatus.FINISH;
    CustomLogger.logGroupState(this);
    CustomLogger.logGroupSettings(this);
    bases.forEach(Base::sendReset);
    humidifiers.forEach(Humidifier::sendReset);
    Mobifume.getInstance().getEventDispatcher().call(PurgeFinishedEvent.create(this));
  }

  public void stop() {
    CustomLogger.info(this, "STOP");
    cancelEvaporateTaskIfScheduled();
    cancelPurgeTaskIfScheduled();
    bases.forEach(Base::sendReset);
    humidifiers.forEach(Humidifier::sendReset);
  }

  public String getName() {
    return name;
  }

  @Override
  public int getCycleNumber() {
    return cycleNumber;
  }

  public List<Filter> getFilters() {
    return Collections.unmodifiableList(filters);
  }

  public GroupStatus getStatus() {
    return status;
  }

  public GroupSettings getSettings() {
    return settings;
  }

  public boolean isHumidifySetpointReached() {
    return humidifySetpointReached;
  }

  public boolean isHumidifying() {
    return humidifying;
  }

  public long getEvaporateStartTime() {
    return evaporateStartTime;
  }

  public long getPurgeStartTime() {
    return purgeStartTime;
  }

  public static class RoomBuilder {

    private String name;
    private Integer cycleNumber;
    private List<Base> bases;
    private List<Humidifier> humidifiers;
    private List<Filter> filters;
    private GroupSettings settings;

    private RoomBuilder() {
    }

    public Room build() {
      checkNotNull(name);
      checkNotNull(cycleNumber);
      checkNotNull(bases);
      checkNotNull(humidifiers);
      checkNotNull(filters);
      checkNotNull(settings);
      return new Room(this);
    }

    public RoomBuilder name(String name) {
      checkNotNull(name);
      checkArgument(!name.isEmpty());
      this.name = name;
      return this;
    }

    public RoomBuilder cycleNumber(int cycleNumber) {
      this.cycleNumber = cycleNumber;
      return this;
    }

    public RoomBuilder bases(List<Base> bases) {
      checkNotNull(bases);
      this.bases = Lists.newArrayList(bases);
      return this;
    }

    public RoomBuilder humidifiers(List<Humidifier> humidifiers) {
      checkNotNull(humidifiers);
      this.humidifiers = Lists.newArrayList(humidifiers);
      return this;
    }

    public RoomBuilder filters(List<Filter> filters) {
      checkNotNull(filters);
      this.filters = Lists.newArrayList(filters);
      return this;
    }

    public RoomBuilder settings(GroupSettings settings) {
      checkNotNull(settings);
      this.settings = settings;
      return this;
    }
  }
}
