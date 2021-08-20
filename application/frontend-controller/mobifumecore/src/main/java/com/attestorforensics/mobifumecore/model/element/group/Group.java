package com.attestorforensics.mobifumecore.model.element.group;

import com.attestorforensics.mobifumecore.model.element.filter.Filter;
import com.attestorforensics.mobifumecore.model.element.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.element.node.Base;
import com.attestorforensics.mobifumecore.model.element.node.Device;
import com.attestorforensics.mobifumecore.model.element.node.Humidifier;
import com.attestorforensics.mobifumecore.model.setting.GroupSettings;
import java.util.List;
import org.apache.log4j.Logger;

public interface Group {

  String getName();

  int getCycleNumber();

  Logger getLogger();

  boolean containsDevice(Device device);

  List<Base> getBases();

  List<Humidifier> getHumidifiers();

  List<Filter> getFilters();

  GroupStatus getStatus();

  GroupSettings getSettings();

  void setSettings(GroupSettings settings);

  DoubleSensor getTemperature();

  DoubleSensor getHumidity();

  void setupStart();

  void startHumidify();

  void startEvaporate();

  void startPurge();

  void updateHumidify();

  boolean isHumidifySetpointReached();

  boolean isHumidifying();

  long getEvaporateStartTime();

  long getPurgeStartTime();

  void cancel();

  void updateHeaterSetpoint();

  void sendState(Device device);

  void updateHeatTimer();

  void resetHeatTimer();

  void updatePurgeTimer();

  void resetPurgeTimer();
}
