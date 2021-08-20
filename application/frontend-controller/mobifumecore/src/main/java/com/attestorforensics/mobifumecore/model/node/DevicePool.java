package com.attestorforensics.mobifumecore.model.node;

import java.util.List;
import java.util.Optional;

public interface DevicePool {

  void addBase(Base base);

  void addHumidifier(Humidifier humidifier);

  void removeBase(Base base);

  void removeHumidifier(Humidifier humidifier);

  Optional<Base> getBase(String deviceId);

  List<Base> getAllBases();

  Optional<Humidifier> getHumidifier(String deviceId);

  List<Humidifier> getAllHumidifier();
}
