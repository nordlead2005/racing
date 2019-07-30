package com.github.hornta.race.enums;

public enum Permission {
  RACING_ADMIN("racing.admin"),
  RACING_MODIFY("racing.modify"),
  RACING_MODERATOR("racing.moderator"),
  RACING_PLAYER("racing.player");

  private String node;

  Permission(String node) {
    this.node = node;
  }

  @Override
  public String toString() {
    return node;
  }
}
