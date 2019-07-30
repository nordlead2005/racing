package com.github.hornta.race.message;

public enum PlaceholderOption {
  DELIMITER;

  public static PlaceholderOption fromString(String string) {
    for(PlaceholderOption value : values()) {
      if(value.name().equalsIgnoreCase(string)) {
        return value;
      }
    }
    return null;
  }
}
