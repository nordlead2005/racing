package com.github.hornta.race.events;

import com.github.hornta.race.objects.RacePlayerSession;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ParticipateEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private RaceSession raceSession;
  private RacePlayerSession playerSession;

  public ParticipateEvent(RaceSession raceSession, RacePlayerSession playerSession) {
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
