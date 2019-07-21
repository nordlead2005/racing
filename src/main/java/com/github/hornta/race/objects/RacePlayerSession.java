package com.github.hornta.race.objects;

import com.github.hornta.race.Racing;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

public class RacePlayerSession {
  private static final int PREVENT_SPRINT_FOOD_LEVEL = 6;
  private static final double MAX_HEALTH = 20;
  private static final int MAX_FOOD_LEVEL = 20;
  private static final double HORSE_JUMP_STRENGTH = 0.7;
  private static final double HORSE_SPEED = 0.225;
  private final Race race;
  private final Player player;
  private final double chargedEntryFee;
  private RaceStartPoint startPoint;
  private Location startLocation;
  private RaceCheckpoint currentCheckpoint;
  private RaceCheckpoint nextCheckpoint;
  private float walkSpeed;
  private int foodLevel;
  private ItemStack[] inventory;
  private Collection<PotionEffect> potionEffects;
  private double health;
  private GameMode gameMode;
  private int fireTicks;
  private BossBar bossBar;
  private Pig pig;
  private Horse horse;
  private Boat boat;
  private int currentLap;
  private boolean isAllowedToEnterVehicle;
  private boolean isAllowedToExitVehicle;
  private boolean isDirty;

  RacePlayerSession(Race race, Player player, double chargedEntryFee) {
    this.race = race;
    this.player = player;
    this.chargedEntryFee = chargedEntryFee;
  }

  public Horse getHorse() {
    return horse;
  }

  public Pig getPig() {
    return pig;
  }

  public Boat getBoat() {
    return boat;
  }

  void startCooldown() {
    walkSpeed = player.getWalkSpeed();
    foodLevel = player.getFoodLevel();
    inventory = player.getInventory().getContents();
    potionEffects = new ArrayList<>(player.getActivePotionEffects());
    health = player.getHealth();
    gameMode = player.getGameMode();
    fireTicks = player.getFireTicks();

    player.setWalkSpeed(0);
    player.setFoodLevel(PREVENT_SPRINT_FOOD_LEVEL);
    player.getInventory().clear();
    for(PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }
    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, RaceCountdown.COUNTDOWN_IN_SECONDS * 20, 128));
    player.setHealth(MAX_HEALTH);
    player.setGameMode(GameMode.ADVENTURE);
    player.setFireTicks(0);
    player.closeInventory();

    // prevent taking damage and playing fall damage particles when teleporting during falling
    player.setFallDistance(0);

    if(player.isInsideVehicle()) {
      isAllowedToExitVehicle = true;
      player.getVehicle().eject();
      isAllowedToExitVehicle = false;
    }

    // always teleport the player regardless of race type
    // because if the player is too far away from the vehicle when entering
    // then sometimes it won't be teleported to the vehicle
    player.teleport(startLocation);

    switch (race.getType()) {
      case PIG:
        spawnPig();
        enterVehicle();
        break;

      case HORSE:
        spawnHorse(true);
        enterVehicle();
        break;

      case ELYTRA:
        MessageManager.sendMessage(player, MessageKey.ELYTRA_INFO);
        break;

      case BOAT:
        spawnBoat();
        enterVehicle();
        break;

      default:
    }

    // play sound after being teleported because else we would be teleported away from the sound location
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);

    isDirty = true;
  }

  Entity getVehicle() {
    if(pig != null) {
      return pig;
    }

    if(horse != null) {
      return horse;
    }

    if(boat != null) {
      return boat;
    }

    return null;
  }

  public double getChargedEntryFee() {
    return chargedEntryFee;
  }

  void startRace() {
    player.setWalkSpeed(race.getWalkSpeed());
    player.setFoodLevel(MAX_FOOD_LEVEL);
    player.removePotionEffect(PotionEffectType.JUMP);

    if(pig != null) {
      player.getInventory().setItemInMainHand(new ItemStack(Material.CARROT_ON_A_STICK, 1));
    }

    if(horse != null) {
      unfreezeHorse();
    }

    if(race.getType() == RaceType.ELYTRA) {
      player.getInventory().setChestplate(new ItemStack(Material.ELYTRA, 1));
    }
  }

  private Location getRespawnLocation() {
    if (currentCheckpoint == null || race.getType() == RaceType.ELYTRA) {
      return startPoint.getLocation();
    } else {
      return currentCheckpoint.getLocation().getWorld().getHighestBlockAt(
        currentCheckpoint.getLocation()
      ).getLocation();
    }
  }

  void respawnOnDeath(EntityDamageEvent event) {
    event.setCancelled(true);
    player.setFoodLevel(MAX_FOOD_LEVEL);
    player.setHealth(MAX_HEALTH);

    if(getVehicle() != null) {
      exitVehicle();
      getVehicle().remove();

      Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
        switch (race.getType()) {
          case PIG:
            spawnPig();
            break;

          case HORSE:
            spawnHorse(false, horse);
            break;

          case BOAT:
            spawnBoat();
            break;

          default:
        }
        player.teleport(getRespawnLocation());
        player.setFireTicks(0);
        enterVehicle();
      }, 1L);
    } else {
      player.teleport(getRespawnLocation());
      player.setFireTicks(0);
    }
  }

  public void respawnInVehicle() {
    if(getVehicle() != null) {
      exitVehicle();
      getVehicle().remove();
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(race.getType() == RaceType.HORSE) {
        spawnHorse(true, horse);
      } else if(race.getType() == RaceType.BOAT) {
        spawnBoat();
      }
      player.teleport(getRespawnLocation());
      enterVehicle();
    }, 1L);
  }

  private void spawnPig() {
    pig = (Pig) startLocation.getWorld().spawnEntity(getRespawnLocation(), EntityType.PIG);
    pig.setInvulnerable(true);
    pig.setAI(false);
    pig.setSaddle(true);
  }

  private void spawnHorse(boolean freeze) {
    spawnHorse(freeze, null);
  }

  private void spawnHorse(boolean freeze, Horse oldHorse) {
    horse = (Horse) startLocation.getWorld().spawnEntity(getRespawnLocation(), EntityType.HORSE);
    horse.setInvulnerable(true);
    horse.setAI(false);
    horse.setTamed(true);
    horse.setOwner(player);
    horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
    horse.setJumpStrength(freeze ? 0 : HORSE_JUMP_STRENGTH);
    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(freeze ? 0 : HORSE_SPEED);

    if(oldHorse != null) {
      horse.setColor(oldHorse.getColor());
      horse.setStyle(oldHorse.getStyle());
      horse.setAge(oldHorse.getAge());
    }
  }

  private void spawnBoat() {
    boat = (Boat) startLocation.getWorld().spawnEntity(getRespawnLocation(), EntityType.BOAT);
    boat.setInvulnerable(true);
    boat.setWoodType(TreeSpecies.GENERIC);
  }

  private void unfreezeHorse() {
    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(HORSE_SPEED);
    horse.setJumpStrength(HORSE_JUMP_STRENGTH);
  }

  void setStartPoint(RaceStartPoint startPoint) {
    this.startPoint = startPoint;
    startLocation = Util.snapAngles(startPoint.getLocation());
  }

  public Location getStartLocation() {
    return startLocation;
  }

  public Player getPlayer() {
    return player;
  }

  boolean isAllowedToEnterVehicle() {
    return isAllowedToEnterVehicle;
  }

  boolean isAllowedToExitVehicle() {
    return isAllowedToExitVehicle;
  }

  void enterVehicle() {
    isAllowedToEnterVehicle = true;
    getVehicle().addPassenger(player);
    isAllowedToEnterVehicle = false;
  }

  void exitVehicle() {
    isAllowedToExitVehicle = true;
    getVehicle().removePassenger(player);
    isAllowedToExitVehicle = false;
  }

  void restore() {
    if(!isDirty) {
      return;
    }

    startPoint = null;
    startLocation = null;
    currentCheckpoint = null;

    // its null when player has finished
    if(nextCheckpoint != null) {
      nextCheckpoint.removePlayer(player);
      nextCheckpoint = null;
    }
    player.setWalkSpeed(walkSpeed);

    for(PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }

    player.addPotionEffects(potionEffects);
    player.setFoodLevel(foodLevel);
    player.getInventory().setContents(inventory);
    player.setHealth(health);
    player.setGameMode(gameMode);
    player.setFireTicks(fireTicks);
    bossBar.removeAll();
    bossBar = null;
    if(getVehicle() != null) {
      exitVehicle();
      getVehicle().remove();
    }
    pig = null;
    horse = null;
    boat = null;

    isDirty = false;
  }

  public RaceCheckpoint getCurrentCheckpoint() {
    return currentCheckpoint;
  }

  public RaceCheckpoint getNextCheckpoint() {
    return nextCheckpoint;
  }

  public void setNextCheckpoint(RaceCheckpoint checkpoint) {
    currentCheckpoint = nextCheckpoint;

    if(currentCheckpoint != null) {
      currentCheckpoint.removePlayer(player);
    }

    nextCheckpoint = checkpoint;

    if(nextCheckpoint != null) {
      nextCheckpoint.addPlayer(player);
    }
  }

  public BossBar getBossBar() {
    return bossBar;
  }

  void setBossBar(BossBar bossBar) {
    bossBar.addPlayer(player);
    this.bossBar = bossBar;
  }

  public int getCurrentLap() {
    return currentLap;
  }

  public void setCurrentLap(int currentLap) {
    this.currentLap = currentLap;
  }
}
