package com.github.hornta.race.objects;

import com.github.hornta.race.enums.RaceCommandType;

public class RaceCommand {
  private RaceCommandType commandType;
  private boolean enabled;
  private String command;
  private int recipient;

  public RaceCommand(RaceCommandType commandType, boolean enabled, String command, int recipient) {
    this.commandType = commandType;
    this.enabled = enabled;
    this.command = command;
    this.recipient = recipient;
  }

  public RaceCommandType getCommandType() {
    return commandType;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getCommand() {
    return command;
  }

  public int getRecipient() {
    return recipient;
  }
}
