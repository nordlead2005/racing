package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.event.HandlerList;

abstract public class RaceStartPointEvent extends RaceEvent {
  private RaceStartPoint startPoint;

  RaceStartPointEvent(Race race, RaceStartPoint startPoint) {
    super(race);

    this.startPoint = startPoint;
  }

  public RaceStartPoint getStartPoint() {
    return startPoint;
  }
}
