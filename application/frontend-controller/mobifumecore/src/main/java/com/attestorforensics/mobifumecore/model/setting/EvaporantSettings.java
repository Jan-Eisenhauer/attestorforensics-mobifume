package com.attestorforensics.mobifumecore.model.setting;

import static com.google.common.base.Preconditions.checkNotNull;

public class EvaporantSettings {

  private static final EvaporantSettings defaultEvaporantSettings =
      builder().evaporantWithItsAmountPerCm(Evaporant.CYANACRYLAT)
          .roomWidth(5)
          .roomDepth(5)
          .roomHeight(2.5)
          .build();

  private final Evaporant evaporant;
  private final double evaporantAmountPerCm;
  private final double roomWidth;
  private final double roomDepth;
  private final double roomHeight;

  private EvaporantSettings(EvaporantSettingsBuilder builder) {
    this.evaporant = builder.evaporant;
    this.evaporantAmountPerCm = builder.evaporantAmountPerCm;
    this.roomWidth = builder.roomWidth;
    this.roomDepth = builder.roomDepth;
    this.roomHeight = builder.roomHeight;
  }

  public static EvaporantSettingsBuilder builder() {
    return new EvaporantSettingsBuilder();
  }

  public static EvaporantSettings getDefault() {
    return defaultEvaporantSettings;
  }

  public Evaporant evaporant() {
    return evaporant;
  }

  public EvaporantSettings evaporant(Evaporant evaporant) {
    return builder().evaporant(evaporant)
        .evaporantAmountPerCm(evaporantAmountPerCm)
        .roomWidth(roomWidth)
        .roomDepth(roomDepth)
        .roomHeight(roomHeight)
        .build();
  }

  public double evaporantAmountPerCm() {
    return evaporantAmountPerCm;
  }

  public EvaporantSettings evaporantAmountPerCm(double evaporantAmountPerCm) {
    return builder().evaporant(evaporant)
        .evaporantAmountPerCm(evaporantAmountPerCm)
        .roomWidth(roomWidth)
        .roomDepth(roomDepth)
        .roomHeight(roomHeight)
        .build();
  }

  public double roomWidth() {
    return roomWidth;
  }

  public EvaporantSettings roomWidth(double roomWidth) {
    return builder().evaporant(evaporant)
        .evaporantAmountPerCm(evaporantAmountPerCm)
        .roomWidth(roomWidth)
        .roomDepth(roomDepth)
        .roomHeight(roomHeight)
        .build();
  }

  public double roomDepth() {
    return roomDepth;
  }

  public EvaporantSettings roomDepth(double roomDepth) {
    return builder().evaporant(evaporant)
        .evaporantAmountPerCm(evaporantAmountPerCm)
        .roomWidth(roomWidth)
        .roomDepth(roomDepth)
        .roomHeight(roomHeight)
        .build();
  }

  public double roomHeight() {
    return roomHeight;
  }

  public EvaporantSettings roomHeight(double roomHeight) {
    return builder().evaporant(evaporant)
        .evaporantAmountPerCm(evaporantAmountPerCm)
        .roomWidth(roomWidth)
        .roomDepth(roomDepth)
        .roomHeight(roomHeight)
        .build();
  }

  public static class EvaporantSettingsBuilder {

    private Evaporant evaporant;
    private Double evaporantAmountPerCm;
    private Double roomWidth;
    private Double roomDepth;
    private Double roomHeight;

    private EvaporantSettingsBuilder() {
    }

    public EvaporantSettings build() {
      checkNotNull(evaporant);
      checkNotNull(evaporantAmountPerCm);
      checkNotNull(roomWidth);
      checkNotNull(roomDepth);
      checkNotNull(roomHeight);
      return new EvaporantSettings(this);
    }

    public EvaporantSettingsBuilder evaporant(Evaporant evaporant) {
      checkNotNull(evaporant);
      this.evaporant = evaporant;
      return this;
    }

    public EvaporantSettingsBuilder evaporantAmountPerCm(double evaporantAmountPerCm) {
      this.evaporantAmountPerCm = evaporantAmountPerCm;
      return this;
    }

    public EvaporantSettingsBuilder evaporantWithItsAmountPerCm(Evaporant evaporant) {
      checkNotNull(evaporant);
      this.evaporant = evaporant;
      this.evaporantAmountPerCm = evaporant.getAmountPerCm();
      return this;
    }

    public EvaporantSettingsBuilder roomWidth(double roomWidth) {
      this.roomWidth = roomWidth;
      return this;
    }

    public EvaporantSettingsBuilder roomDepth(double roomDepth) {
      this.roomDepth = roomDepth;
      return this;
    }

    public EvaporantSettingsBuilder roomHeight(double roomHeight) {
      this.roomHeight = roomHeight;
      return this;
    }
  }
}
