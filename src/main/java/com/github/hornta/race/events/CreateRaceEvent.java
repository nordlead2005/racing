package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import org.bukkit.event.HandlerList;

public class CreateRaceEvent extends RaceEvent {
  private static final HandlerList handlers = new HandlerList();

  public CreateRaceEvent(Race race) {
    super(race);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
