package com.attestorforensics.mobifumecore.model.object;

import com.attestorforensics.mobifumecore.model.setting.Settings;
import java.util.List;
import org.apache.log4j.Logger;

public interface Group {

  String getName();

  Logger getLogger();

  List<Device> getDevices();

  boolean containsDevice(Device device);

  List<Base> getBases();

  List<Humidifier> getHumidifiers();

  List<Filter> getFilters();

  Status getStatus();

  Settings getSettings();

  void setSettings(Settings settings);

  double getTemperature();

  double getHumidity();

  void setupStart();

  void startHumidify();

  void startEvaporate();

  void startPurge();

  void updateHumidify();

  boolean isHumidifyMaxReached();

  boolean isHumidifying();

  long getEvaporateStartTime();

  long getPurgeStartTime();

  void reset();

  void cancel();

  void updateHeaterSetpoint();

  void sendState(Device device);

  void updateHeatTimer();

  void resetHeatTimer();

  void updatePurgeTimer();

  void resetPurgeTimer();
}
