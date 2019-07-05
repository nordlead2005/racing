package com.github.hornta.race.objects;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Location;

import java.util.UUID;

public class RacePoint {
  private Location location;
  private int position;
  private UUID id;
  private Hologram hologram;

  public RacePoint(UUID id, int position, Location location) {
    this.id = id;
    this.position = position;
    this.location = location;
  }

  public Hologram getHologram() {
    return hologram;
  }

  public void setHologram(Hologram hologram) {
    this.hologram = hologram;
  }

  public UUID getId() {
    return id;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public Location getLocation() {
    return location.clone();
  }
}
