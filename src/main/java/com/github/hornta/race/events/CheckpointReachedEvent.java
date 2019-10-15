package com.github.hornta.race.events;

import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RacePlayerSession;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CheckpointReachedEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private RaceSession raceSession;
  private RacePlayerSession playerSession;
  private RaceCheckpoint checkpoint;

  public CheckpointReachedEvent(
    RaceSession raceSession,
    RacePlayerSession playerSession,
    RaceCheckpoint checkpoint
  ) {
    this.raceSession = raceSession;
    this.playerSession = playerSession;
    this.checkpoint = checkpoint;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public RaceSession getRaceSession() {
    return raceSession;
  }

  public RacePlayerSession getPlayerSession() {
    return playerSession;
  }

  public RaceCheckpoint getCheckpoint() {
    return checkpoint;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
