package com.github.hornta.race.objects;

import com.github.hornta.race.enums.RacingType;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Race implements Listener {
  private UUID id;
  private String version;
  private String name;
  private Location spawn;
  private List<RaceCheckpoint> checkpoints;
  private List<RaceStartPoint> startPoints;
  private boolean isEnabled;
  private boolean isEditing;
  private Instant createdAt;
  private RacingType type;
  private String song;

  public Race(
    UUID id,
    String version,
    String name,
    Location spawn,
    boolean isEnabled,
    boolean isEditing,
    Instant createdAt,
    List<RaceCheckpoint> checkpoints,
    List<RaceStartPoint> startPoints,
    RacingType type,
    String song
  ) {
    this.id = id;
    this.name = name;
    this.spawn = spawn;
    this.isEnabled = isEnabled;
    this.isEditing = isEditing;
    this.createdAt = createdAt;
    this.checkpoints = new ArrayList<>(checkpoints);
    this.startPoints = new ArrayList<>(startPoints);
    this.type = type;
    this.song = song;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Location getSpawn() {
    return spawn.clone();
  }

  public void setSpawn(Location spawn) {
    this.spawn = spawn;
  }

  public List<RaceCheckpoint> getCheckpoints() {
    return new ArrayList<>(checkpoints);
  }

  public List<RaceStartPoint> getStartPoints() {
    return new ArrayList<>(startPoints);
  }

  public RaceCheckpoint getCheckpoint(int position) {
    for(RaceCheckpoint checkpoint : checkpoints) {
      if(checkpoint.getPosition() == position) {
        return checkpoint;
      }
    }
    return null;
  }

  public RaceCheckpoint getCheckpoint(Location location) {
    for(RaceCheckpoint checkpoint : checkpoints) {
      if(
        checkpoint.getLocation().getBlockX() == location.getBlockX() &&
        checkpoint.getLocation().getBlockY() == location.getBlockY() &&
        checkpoint.getLocation().getBlockZ() == location.getBlockZ()) {
        return checkpoint;
      }
    }
    return null;
  }

  public RaceStartPoint getStartPoint(int position) {
    for(RaceStartPoint startPoint : startPoints) {
      if(startPoint.getPosition() == position) {
        return startPoint;
      }
    }
    return null;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public void setEnabled(boolean enabled) {
    isEnabled = enabled;
  }

  public boolean isEditing() {
    return isEditing;
  }

  public void setEditing(boolean editing) {
    isEditing = editing;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void addStartPoint(RaceCheckpoint point) {
    checkpoints.add(point);
  }

  public void delPoint(RaceCheckpoint point) {
    checkpoints.remove(point);
  }

  public void deleteStartPoint(RaceStartPoint startPoint) {
    startPoints.remove(startPoint);
  }

  public void addStartPoint(RaceStartPoint startPoint) {
    startPoints.add(startPoint);
  }

  public RacingType getType() {
    return type;
  }

  public void setType(RacingType type) {
    this.type = type;
  }

  public String getSong() {
    return song;
  }

  public void setSong(String song) {
    this.song = song;
  }
}
