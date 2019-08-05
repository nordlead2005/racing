package com.github.hornta.race.objects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.time.Instant;
import java.util.UUID;

public class RaceSign {
  private final Sign sign;
  private final UUID creator;
  private final Instant createdAt;

  public RaceSign(Sign sign, UUID creator, Instant createdAt) {
    this.sign = sign;
    this.creator = creator;
    this.createdAt = createdAt;
  }

  public Sign getSign() {
    return sign;
  }

  public UUID getCreator() {
    return creator;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getKey() {
    return createKey(sign.getBlock());
  }

  public static String createKey(Block block) {
    return block.getLocation().getWorld().getName() +
      "_" + block.getLocation().getBlockX() +
      "_" + block.getLocation().getBlockY() +
      "_" + block.getLocation().getBlockZ();
  }
}
