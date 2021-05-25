package com.attestorforensics.mobifumecore.view;

import lombok.Getter;

/**
 * Defines the different colors for a group.
 */
public enum GroupColor {

  DEEP_ORANGE("#ff7043"),
  LIGHT_GREEN("#9ccc65"),
  PURPLE("#ab47bc"),
  YELLOW("#ffee58"),
  TEAL("#26a69a"),
  BROWN("#8d6e63"),
  PINK("#ec407a");

  private static int currentColor = 0;
  @Getter
  private String color;

  GroupColor(String color) {
    this.color = color;
  }

  /**
   * Gets the next group color. It is repeating, when all colors are used.
   *
   * @return the next color
   */
  public static String getNextColor() {
    int length = GroupColor.values().length;
    if (currentColor >= length) {
      currentColor -= length;
    }
    String color = GroupColor.values()[currentColor].getColor();
    currentColor++;
    return color;
  }
}
