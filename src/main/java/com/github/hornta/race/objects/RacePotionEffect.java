package com.github.hornta.race.objects;

import org.bukkit.potion.PotionEffectType;

public class RacePotionEffect {
  private final PotionEffectType type;
  private final int amplifier;

  public RacePotionEffect(PotionEffectType type, int amplifier) {
    this.type = type;
    this.amplifier = amplifier;
  }

  public PotionEffectType getType() {
    return type;
  }

  public int getAmplifier() {
    return amplifier;
  }
}
