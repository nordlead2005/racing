package com.github.hornta.race.objects;

import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RaceType;
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
  private RaceState state;
  private Instant createdAt;
  private RaceType type;
  private String song;

  public Race(
    UUID id,
    String version,
    String name,
    Location spawn,
    RaceState state,
    Instant createdAt,
    List<RaceCheckpoint> checkpoints,
    List<RaceStartPoint> startPoints,
    RaceType type,
    String song
  ) {
    this.id = id;
    this.version = version;
    this.name = name;
    this.spawn = spawn;
    this.state = state;
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

  public RaceStartPoint getStartPoint(Location location) {
    for(RaceStartPoint startPoint : startPoints) {
      if(
        startPoint.getLocation().getBlockX() == location.getBlockX() &&
        startPoint.getLocation().getBlockY() == location.getBlockY() &&
        startPoint.getLocation().getBlockZ() == location.getBlockZ()
      ) {
        return startPoint;
      }
    }
    return null;
  }

  public RaceState getState() {
    return state;
  }

  public void setState(RaceState state) {
    this.state = state;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void addStartPoint(RaceCheckpoint point) {
    checkpoints.add(point);
  }

  public void addStartPoint(RaceStartPoint startPoint) {
    startPoints.add(startPoint);
  }

  public void setStartPoints(List<RaceStartPoint> startPoints) {
    this.startPoints = startPoints;
  }

  public void setCheckpoints(List<RaceCheckpoint> checkpoints) {
    this.checkpoints = checkpoints;
  }

  public RaceType getType() {
    return type;
  }

  public void setType(RaceType type) {
    this.type = type;
  }

  public String getSong() {
    return song;
  }

  public void setSong(String song) {
    this.song = song;
  }
}
