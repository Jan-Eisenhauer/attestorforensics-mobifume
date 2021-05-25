package com.attestorforensics.mobifumecore.model.object;

public interface Run {

  int getCycle();

  long getDate();

  Evaporant getEvaporant();

  double getPercentage();

  boolean isManuallyAdded();
}
