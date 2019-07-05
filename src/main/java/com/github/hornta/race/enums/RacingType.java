package com.github.hornta.race.enums;

public enum RacingType {
  PLAYER,
  HORSE,
  PIG,
  ELYTRA;

  public static RacingType fromString(String string) {
    for(RacingType type : values()) {
      if(type.name().compareToIgnoreCase(string) == 0) {
        return type;
      }
    }
    return null;
  };
}
