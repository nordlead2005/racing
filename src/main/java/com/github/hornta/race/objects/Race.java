package com.github.hornta.race.objects;

import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.*;

public class Race implements Listener {
  private final UUID id;
  private RaceVersion version;
  private String name;
  private Location spawn;
  private List<RaceCheckpoint> checkpoints;
  private List<RaceStartPoint> startPoints;
  private RaceState state;
  private Instant createdAt;
  private RaceType type;
  private String song;
  private double entryFee;
  private float walkSpeed;
  private Set<RacePotionEffect> potionEffects;

  public Race(
    UUID id,
    RaceVersion version,
    String name,
    Location spawn,
    RaceState state,
    Instant createdAt,
    List<RaceCheckpoint> checkpoints,
    List<RaceStartPoint> startPoints,
    RaceType type,
    String song,
    double entryFee,
    float walkSpeed,
    Set<RacePotionEffect> potionEffects
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
    this.entryFee = entryFee;
    this.walkSpeed = walkSpeed;
    this.potionEffects = potionEffects;
  }

  public Set<RacePotionEffect> getPotionEffects() {
    return potionEffects;
  }

  public void addPotionEffect(RacePotionEffect potionEffect) {
    removePotionEffect(potionEffect.getType());
    potionEffects.add(potionEffect);
  }

  public void removePotionEffect(PotionEffectType type) {
    Iterator it = potionEffects.iterator();
    while(it.hasNext()) {
      if(((RacePotionEffect)it.next()).getType() == type) {
        it.remove();
        return;
      }
    }
  }

  public void clearPotionEffects() {
    potionEffects.clear();
  }

  public float getWalkSpeed() {
    return walkSpeed;
  }

  public void setWalkSpeed(float walkSpeed) {
    this.walkSpeed = walkSpeed;
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

  public double getEntryFee() {
    return entryFee;
  }

  public void setEntryFee(double entryFee) {
    this.entryFee = entryFee;
  }

  public RaceVersion getVersion() {
    return version;
  }
}
