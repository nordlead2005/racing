package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import org.bukkit.event.HandlerList;

abstract public class RaceCheckpointEvent extends RaceEvent {
  private static final HandlerList handlers = new HandlerList();
  private RaceCheckpoint checkpoint;

  RaceCheckpointEvent(Race race, RaceCheckpoint checkpoint) {
    super(race);

    this.checkpoint = checkpoint;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public RaceCheckpoint getCheckpoint() {
    return checkpoint;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
