package com.attestorforensics.mobifumecore.model.object;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunProcess implements Run {

  private int cycle;
  private long date;
  private String evaporant;
  private double evaporantAmount;
  private int totalFilterCount;
  private boolean manually;

  RunProcess(int cycle, long date, Evaporant evaporant, double evaporantAmount,
      int totalFilterCount, boolean manually) {
    this.cycle = cycle;
    this.date = date;
    this.evaporant = evaporant.name();
    this.evaporantAmount = evaporantAmount;
    this.totalFilterCount = totalFilterCount;
    this.manually = manually;
  }

  @Override
  public int getCycle() {
    return cycle;
  }

  @Override
  public long getDate() {
    return date;
  }

  @Override
  public Evaporant getEvaporant() {
    return Evaporant.valueOf(evaporant);
  }

  @Override
  public double getPercentage() {
    return (1 / getEvaporant().cycles) * evaporantAmount / totalFilterCount;
  }

  @Override
  public boolean isManuallyAdded() {
    return manually;
  }
}
