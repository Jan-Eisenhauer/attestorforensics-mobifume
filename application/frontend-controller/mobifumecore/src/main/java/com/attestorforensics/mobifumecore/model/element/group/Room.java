package com.attestorforensics.mobifumecore.model.element.group;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.log.CustomLogger;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import org.apache.log4j.Logger;

public class Room implements Group {

  private final Logger logger;
  private final String name;
  private final int cycleNumber;
  private final List<Base> bases;
  private final List<Humidifier> humidifiers;
  private final List<Filter> filters;
  private final GroupProcess groupProcess;

  private Room(RoomBuilder roomBuilder) {
    logger = CustomLogger.createGroupLogger(this);
    this.name = roomBuilder.name;
    this.cycleNumber = roomBuilder.cycleNumber;
    this.bases = roomBuilder.bases;
    this.humidifiers = roomBuilder.humidifiers;
    this.filters = roomBuilder.filters;
    this.groupProcess = RoomProcess.create(this, roomBuilder.settings);

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
  public String getName() {
    return name;
  }

  @Override
  public int getCycleNumber() {
    return cycleNumber;
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
  public List<Filter> getFilters() {
    return Collections.unmodifiableList(filters);
  }

  @Override
  public DoubleSensor getAverageTemperature() {
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
  public DoubleSensor getAverageHumidity() {
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
  public GroupProcess getProcess() {
    return groupProcess;
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
