package com.attestorforensics.mobifumecore.model.element.filter;

import com.attestorforensics.mobifumecore.model.element.misc.Evaporant;

public interface Run {

  int getCycle();

  long getDate();

  Evaporant getEvaporant();

  double getPercentage();

  boolean isManuallyAdded();
}
