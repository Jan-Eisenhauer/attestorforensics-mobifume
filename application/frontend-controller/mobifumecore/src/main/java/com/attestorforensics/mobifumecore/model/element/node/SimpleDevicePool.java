package com.attestorforensics.mobifumecore.model.element.node;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.compress.utils.Lists;

public class SimpleDevicePool implements DevicePool {

  private final List<Base> bases = Lists.newArrayList();
  private final List<Humidifier> humidifiers = Lists.newArrayList();

  private SimpleDevicePool() {
  }

  public static SimpleDevicePool create() {
    return new SimpleDevicePool();
  }

  @Override
  public void addBase(Base base) {
    bases.add(base);
  }

  @Override
  public void addHumidifier(Humidifier humidifier) {
    humidifiers.add(humidifier);
  }

  @Override
  public void removeBase(Base base) {
    bases.remove(base);
  }

  @Override
  public void removeHumidifier(Humidifier humidifier) {
    humidifiers.remove(humidifier);
  }

  @Override
  public Optional<Base> getBase(String deviceId) {
    return bases.stream().filter(base -> base.getDeviceId().equals(deviceId)).findFirst();
  }

  @Override
  public List<Base> getAllBases() {
    return ImmutableList.copyOf(bases);
  }

  @Override
  public Optional<Humidifier> getHumidifier(String deviceId) {
    return humidifiers.stream()
        .filter(humidifier -> humidifier.getDeviceId().equals(deviceId))
        .findFirst();
  }

  @Override
  public List<Humidifier> getAllHumidifier() {
    return ImmutableList.copyOf(humidifiers);
  }
}
