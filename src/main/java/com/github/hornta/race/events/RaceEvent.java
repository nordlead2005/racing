package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import org.bukkit.event.Event;

abstract public class RaceEvent extends Event {
  private Race race;

  RaceEvent(Race race) {
    this.race = race;
  }

  public Race getRace() {
    return race;
  }
}



