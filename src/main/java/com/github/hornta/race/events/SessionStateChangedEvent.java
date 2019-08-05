package com.github.hornta.race.events;

import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SessionStateChangedEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private RaceSession raceSession;
  private RaceSessionState oldState;

  public SessionStateChangedEvent(RaceSession raceSession, RaceSessionState oldState) {
    this.raceSession = raceSession;
    this.oldState = oldState;
  }

  public RaceSessionState getOldState() {
    return oldState;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public RaceSession getRaceSession() {
    return raceSession;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
