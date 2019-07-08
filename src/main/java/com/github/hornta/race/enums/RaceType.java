package com.github.hornta.race.enums;

public enum RaceType {
  PLAYER,
  HORSE,
  PIG,
  ELYTRA,
  BOAT;

  public static RaceType fromString(String string) {
    for(RaceType type : values()) {
      if(type.name().compareToIgnoreCase(string) == 0) {
        return type;
      }
    }
    return null;
  }
}
