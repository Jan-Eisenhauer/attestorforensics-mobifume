package com.attestorforensics.mobifumecore.model.setting;

public enum Evaporant {

  CYANACRYLAT(450, 0.5),
  LUMICYANO(450, 1),
  CYANOPOWDER(450, 1),
  POLYCYANO(450, 1),
  PEKA(450, 1);

  double cycles;

  double amountPerCm;

  Evaporant(double cycles, double amountPerCm) {
    this.cycles = cycles;
    this.amountPerCm = amountPerCm;
  }

  public double getCycles() {
    return cycles;
  }

  public double getAmountPerCm() {
    return amountPerCm;
  }
}
