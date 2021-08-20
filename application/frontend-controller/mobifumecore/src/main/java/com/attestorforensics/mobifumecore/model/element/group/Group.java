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

  Logger getLogger();

  String getName();

  int getCycleNumber();

  boolean containsBase(Base base);

  boolean containsHumidifier(Humidifier humidifier);

  List<Base> getBases();

  List<Humidifier> getHumidifiers();

  List<Filter> getFilters();

  DoubleSensor getAverageTemperature();

  DoubleSensor getAverageHumidity();

  GroupProcess getProcess();
}
