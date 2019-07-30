package com.github.hornta.race.objects;

import com.comphenix.protocol.wrappers.WrappedParticle;
import com.github.hornta.race.enums.Permission;
import com.github.hornta.race.Racing;
import com.github.hornta.race.packetwrapper.WrapperPlayServerWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

public class CheckpointParticleTask extends BukkitRunnable {
  private static final double ANGLE_INCREMENT = Math.toRadians(4);
  private static final int NUMBER_ORBS = 2;
  private static final double ANGLE_OFFSET = (2 * Math.PI) / NUMBER_ORBS;
  private double t;
  private RaceCheckpoint checkpoint;
  private boolean isEditing;

  public CheckpointParticleTask(RaceCheckpoint checkpoint, boolean isEditing) {
    this.checkpoint = checkpoint;
    this.isEditing = isEditing;
  }

  @Override
  public void run() {
    List<Player> players = checkpoint.getPlayers();

    boolean isInside = false;
    if(isEditing) {
      for(Player player : Bukkit.getOnlinePlayers()) {
        if(checkpoint.isInside(player)) {
          isInside = true;
          break;
        }
      }
    }

    Vector dir = checkpoint.getLocation().getDirection();

    for(int i = 0; i < NUMBER_ORBS; ++i) {
      double offset = i * ANGLE_OFFSET;
      double angle = t + offset;

      // rotate dir by 90 degrees CW on the y-axis
      Vector p = new Vector(-dir.getZ(), dir.getY(), dir.getX());
      Vector c = p.crossProduct(dir);
      Vector v = c.rotateAroundAxis(dir, angle).normalize().multiply(checkpoint.getRadius());

      Location loc = checkpoint.getLocation().add(v);

      WrapperPlayServerWorldParticles wpswp = new WrapperPlayServerWorldParticles();
      wpswp.setNumberOfParticles(1);
      wpswp.setLongDistance(true);
      wpswp.setParticleType(WrappedParticle.create(Particle.REDSTONE, new Particle.DustOptions(
        Color.fromRGB(isInside ? 0 : 255, isInside ? 255 : 0, 0), 2
      )));
      wpswp.setX((float)loc.getX());
      wpswp.setY((float)loc.getY());
      wpswp.setZ((float)loc.getZ());
      wpswp.setParticleData(2);

      if(isEditing) {
        for(Player player : Bukkit.getOnlinePlayers()) {
          if(!player.hasPermission(Permission.RACING_MODIFY.toString())) {
            continue;
          }

          try {
            Racing.getInstance().getProtocolManager().sendServerPacket(player, wpswp.getHandle());
          } catch (InvocationTargetException e) {
            Racing.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
          }
        }
      } else {
        for (Player player : players) {
          try {
            Racing.getInstance().getProtocolManager().sendServerPacket(player, wpswp.getHandle());
          } catch (InvocationTargetException e) {
            Racing.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
          }
        }
      }
    }
    t += ANGLE_INCREMENT;
  }
}
