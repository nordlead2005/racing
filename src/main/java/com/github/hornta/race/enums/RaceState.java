package com.github.hornta.race.enums;

public enum RaceState {
  ENABLED,
  DISABLED,
  UNDER_CONSTRUCTION;

  public static RaceState fromString(String string) {
    for(RaceState state : values()) {
      if(state.name().compareToIgnoreCase(string) == 0) {
        return state;
      }
    }
    return null;
  }
}
