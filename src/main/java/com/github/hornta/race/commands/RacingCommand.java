package com.github.hornta.race.commands;

import com.github.hornta.race.RacingManager;

public abstract class RacingCommand {
  protected RacingManager racingManager;

  public RacingCommand(RacingManager racingManager) {
    this.racingManager = racingManager;
  }
}
