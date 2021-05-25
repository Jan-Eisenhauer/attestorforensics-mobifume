package com.attestorforensics.mobifumecore.model.object;

import lombok.Getter;

public enum Evaporant {

  CYANACRYLAT(450, 0.5),
  LUMICYANO(450, 1),
  CYANOPOWDER(450, 1),
  POLYCYANO(450, 1),
  PEKA(450, 1);

  @Getter
  double cycles;

  @Getter
  double amountPerCm;

  Evaporant(double cycles, double amountPerCm) {
    this.cycles = cycles;
    this.amountPerCm = amountPerCm;
  }
}
