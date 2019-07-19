package com.github.hornta.race.events;

import com.github.hornta.race.enums.DisqualifyReason;
import com.github.hornta.race.objects.RacePlayerSession;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDisqualifiedEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final RacePlayerSession session;
  private final DisqualifyReason reason;

  public PlayerDisqualifiedEvent(RacePlayerSession session, DisqualifyReason reason) {
    this.session = session;
    this.reason = reason;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public RacePlayerSession getSession() {
    return session;
  }

  public DisqualifyReason getReason() {
    return reason;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
