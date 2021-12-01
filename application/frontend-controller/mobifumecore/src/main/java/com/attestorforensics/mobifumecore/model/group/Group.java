package com.attestorforensics.mobifumecore.model.group;

import com.attestorforensics.mobifumecore.model.filter.Filter;
import com.attestorforensics.mobifumecore.model.node.misc.DoubleSensor;
import com.attestorforensics.mobifumecore.model.node.Base;
import com.attestorforensics.mobifumecore.model.node.Humidifier;
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
