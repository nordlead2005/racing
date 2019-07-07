package com.github.hornta.race.objects;

import com.github.hornta.race.Racing;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RaceCheckpoint extends RacePoint implements Comparable<RaceCheckpoint> {
  private int radius;
  private CheckpointParticleTask task;
  private Vector vector;
  private List<Player> players = new ArrayList<>();

  public RaceCheckpoint(UUID id, int position, Location location, int radius) {
    super(id, position, location);
    this.radius = radius;
    vector = location.toVector();
  }

  public int getRadius() {
    return radius;
  }

  public void stopTask() {
    if(task == null) {
      return;
    }

    task.cancel();
    task = null;
  }

  public void startTask() {
    startTask(false);
  }

  public void startTask(boolean isEditing) {
    task = new CheckpointParticleTask(this, isEditing);
    task.runTaskTimerAsynchronously(Racing.getInstance(), 0, 1L);
  }

  public boolean isInside(Entity entity) {
    return entity.getLocation().toVector().isInSphere(vector, radius);
  }

  public void addPlayer(Player player) {
    players.add(player);
  }

  public void removePlayer(Player player) {
    players.remove(player);
  }

  public List<Player> getPlayers() {
    return new ArrayList<>(players);
  }

  @Override
  public int compareTo(RaceCheckpoint o) {
    return Integer.compare(getPosition(), o.getPosition());
  }
}
