package com.attestorforensics.mobifumecore.model.element.filter;

import com.attestorforensics.mobifumecore.model.element.misc.Evaporant;
import java.util.List;

public interface Filter {

  String getId();

  long getAddedDate();

  double getPercentage();

  int getApproximateUsagesLeft();

  List<Run> getRuns();

  void addRun(int cycle, Evaporant evaporant, double evaporantAmount, int totalFilterCount);

  boolean isPercentageWarning();

  boolean isTimeWarning();

  boolean isOutOfTime();

  boolean isUsable();

  boolean isRemoved();
}
