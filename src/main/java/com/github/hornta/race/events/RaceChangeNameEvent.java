package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import org.bukkit.event.HandlerList;

public class RaceChangeNameEvent extends RaceEvent {
  private static final HandlerList handlers = new HandlerList();
  private String oldName;

  public RaceChangeNameEvent(Race race, String oldName) {
    super(race);
    this.oldName = oldName;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public String getOldName() {
    return oldName;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
