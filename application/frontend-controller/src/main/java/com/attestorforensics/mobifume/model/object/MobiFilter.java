package com.attestorforensics.mobifume.model.object;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MobiFilter implements Filter {

  private final transient long warningTime = 1000L * 60 * 60 * 24 * 30 * 11; // ~ 11 months
  private final transient long outOfTime = 1000L * 60 * 60 * 24 * 365; // ~ 12 months
  private final String id;
  private final long date;
  private transient FilterFileHandler fileHandler;
  private List<RunProcess> runs = new ArrayList<>();
  private boolean removed;

  public MobiFilter(FilterFileHandler fileHandler, String id) {
    this.fileHandler = fileHandler;
    this.id = id;
    date = System.currentTimeMillis();
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public long getAddedDate() {
    return date;
  }

  @Override
  public double getPercentage() {
    double percentage = runs.stream().mapToDouble(Run::getPercentage).sum();
    if (percentage > 1) {
      percentage = 1;
    }
    return percentage;
  }

  @Override
  public int getApproximateUsagesLeft() {
    double percentage = getPercentage();
    int usages = runs.size();
    if (percentage == 0) {
      return -1;
    }
    return (int) (usages / percentage - usages);
  }

  @Override
  public List<Run> getRuns() {
    return runs.stream().map(run -> (Run) run).collect(Collectors.toList());
  }

  @Override
  public void addRun(int cycle, Evaporant evaporant, double evaporantAmount, int totalFilterCount) {
    RunProcess run = new RunProcess(cycle, System.currentTimeMillis(), evaporant, evaporantAmount,
        totalFilterCount, false);
    runs.add(run);
    fileHandler.saveFilter(this);
  }

  @Override
  public boolean isPercentageWarning() {
    return getPercentage() >= 0.9;
  }

  @Override
  public boolean isTimeWarning() {
    long lifeTime = (System.currentTimeMillis() - date);
    return lifeTime > warningTime;
  }

  @Override
  public boolean isOutOfTime() {
    long lifeTime = System.currentTimeMillis() - date;
    return lifeTime > outOfTime;
  }

  @Override
  public boolean isUsable() {
    if (getPercentage() == 1) {
      return false;
    }
    if (isOutOfTime()) {
      long outOfTimeDate = date + outOfTime;
      return getRuns().stream().noneMatch(run -> run.getDate() > outOfTimeDate);
    }
    return true;
  }

  @Override
  public boolean isRemoved() {
    return removed;
  }

  public void setFileHandler(FilterFileHandler fileHandler) {
    this.fileHandler = fileHandler;
  }

  public void setRemoved() {
    removed = true;
    fileHandler.saveFilter(this);
  }
}
