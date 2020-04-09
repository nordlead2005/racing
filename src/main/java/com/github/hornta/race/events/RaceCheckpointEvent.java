package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;

abstract public class RaceCheckpointEvent extends RaceEvent {
  private RaceCheckpoint checkpoint;

  RaceCheckpointEvent(Race race, RaceCheckpoint checkpoint) {
    super(race);

    this.checkpoint = checkpoint;
  }

  public RaceCheckpoint getCheckpoint() {
    return checkpoint;
  }
}
