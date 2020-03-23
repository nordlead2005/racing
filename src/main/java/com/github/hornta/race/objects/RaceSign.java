package com.github.hornta.race.objects;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.time.Instant;
import java.util.UUID;

import com.github.hornta.race.enums.RaceSignType;

public class RaceSign {

  private final Sign sign;
  private final UUID creator;
  private final Instant createdAt;
  private final int laps; //not used for fastest_lap or stats
  private final RaceSignType type;

  public RaceSign(Sign sign, UUID creator, Instant createdAt, int laps, RaceSignType type) {
    this.sign = sign;
    this.creator = creator;
    this.createdAt = createdAt;
    this.laps = laps;
    this.type = type;
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

  public int getLaps() {
    return laps;
  }

  public RaceSignType getSignType() {
    return type;
  }

  public static String createKey(Block block) {
    return block.getLocation().getWorld().getName() +
      "_" + block.getLocation().getBlockX() +
      "_" + block.getLocation().getBlockY() +
      "_" + block.getLocation().getBlockZ();
  }
}
