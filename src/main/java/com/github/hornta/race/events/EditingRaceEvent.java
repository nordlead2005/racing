package com.github.hornta.race.events;

import com.github.hornta.race.objects.Race;
import org.bukkit.event.HandlerList;

public class EditingRaceEvent extends RaceEvent {
  private static final HandlerList handlers = new HandlerList();
  private boolean isEditing;

  public EditingRaceEvent(Race race, boolean isEditing) {
    super(race);
    this.isEditing = isEditing;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public boolean isEditing() {
    return isEditing;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
