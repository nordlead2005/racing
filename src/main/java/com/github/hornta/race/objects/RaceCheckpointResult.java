package com.github.hornta.race.objects;

import org.bukkit.Location;

public class RaceCheckpointResult {
  private RaceCheckpoint checkpoint;
  private int ticks;
  private Location location;

  public RaceCheckpointResult(RaceCheckpoint checkpoint, int ticks, Location location) {
    this.checkpoint = checkpoint;
    this.ticks = ticks;
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  public RaceCheckpoint getCheckpoint() {
    return checkpoint;
  }

  public int getTicks() {
    return ticks;
  }
}
