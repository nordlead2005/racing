package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.event.HandlerList;

public class AddRaceStartPointEvent extends RaceStartPointEvent {
  private static final HandlerList handlers = new HandlerList();

  public AddRaceStartPointEvent(Race race, RaceStartPoint startPoint) {
    super(race, startPoint);
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}

