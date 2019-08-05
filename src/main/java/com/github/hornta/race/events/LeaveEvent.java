package com.github.hornta.race.events;

import com.github.hornta.race.objects.RacePlayerSession;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LeaveEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private RaceSession raceSession;
  private RacePlayerSession playerSession;

  public LeaveEvent(RaceSession raceSession, RacePlayerSession playerSession) {
    this.raceSession = raceSession;
    this.playerSession = playerSession;
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

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
