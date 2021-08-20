package com.attestorforensics.mobifumecore.model.filter;

import com.attestorforensics.mobifumecore.model.setting.Evaporant;

public interface Run {

  int getCycle();

  long getDate();

  Evaporant getEvaporant();

  double getPercentage();

  boolean isManuallyAdded();
}
