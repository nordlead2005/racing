package com.github.hornta.race.objects;

import com.github.hornta.race.ConfigKey;
import com.github.hornta.race.Racing;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.enums.RespawnType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class RacePlayerSession {
  private static final int PREVENT_SPRINT_FOOD_LEVEL = 6;
  public static final double MAX_HEALTH = 20;
  public static final int MAX_FOOD_LEVEL = 20;
  private final Race race;
  private final double chargedEntryFee;
  private final UUID playerId;
  private final String playerName;
  private Player player;
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
  private boolean allowFlight;
  private BossBar bossBar;
  private Pig pig;
  private Horse horse;
  private Boat boat;
  private Minecart minecart;
  private int currentLap;
  private boolean isAllowedToEnterVehicle;
  private boolean isAllowedToExitVehicle;
  private boolean isRestored = true;

  RacePlayerSession(Race race, Player player, double chargedEntryFee) {
    this.race = race;
    this.player = player;
    this.chargedEntryFee = chargedEntryFee;
    this.playerId = player.getUniqueId();
    this.playerName = player.getName();
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public boolean hasPlayer() {
    return player != null;
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public Horse getHorse() {
    return horse;
  }

  public Boat getBoat() {
    return boat;
  }

  public Minecart getMinecart() {
    return minecart;
  }

  void startCooldown() {
    walkSpeed = player.getWalkSpeed();
    foodLevel = player.getFoodLevel();
    inventory = player.getInventory().getContents();
    potionEffects = new ArrayList<>(player.getActivePotionEffects());
    health = player.getHealth();
    gameMode = player.getGameMode();
    fireTicks = player.getFireTicks();
    allowFlight = player.getAllowFlight();

    player.setWalkSpeed(0);
    player.setFoodLevel(PREVENT_SPRINT_FOOD_LEVEL);
    player.getInventory().clear();
    for(PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
    }

    // prevent players from jumping during the countdown
    int countdown = Racing.getInstance().getConfiguration().get(ConfigKey.COUNTDOWN);
    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, countdown * 20, 128));

    if(Racing.getInstance().getConfiguration().get(ConfigKey.ADVENTURE_ON_START)) {
      player.setGameMode(GameMode.ADVENTURE);
    }
    player.closeInventory();

    if(player.isInsideVehicle()) {
      isAllowedToExitVehicle = true;
      player.getVehicle().eject();
      isAllowedToExitVehicle = false;
    }

    switch (race.getType()) {
      case HORSE:
      case BOAT:
      case PIG:
      case MINECART:
        player.setAllowFlight(true);
        break;
      case ELYTRA:
      case PLAYER:
        player.setAllowFlight(false);
        break;
      default:
    }

    respawn(RespawnType.FROM_START, () -> {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
    }, () -> player.setHealth(MAX_HEALTH));

    if(race.getType() == RaceType.HORSE) {
      freezeHorse();
    }

    isRestored = false;
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

    if(minecart != null) {
      return minecart;
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

  public void respawnInVehicle() {
    Location location;
    if (currentCheckpoint == null || race.getType() == RaceType.ELYTRA) {
      location = startLocation;
    } else {
      location = currentCheckpoint.getLocation().getWorld().getHighestBlockAt(
        currentCheckpoint.getLocation()
      ).getLocation();
    }

    respawnInVehicle(location, null, null);
  }

  public void respawnInVehicle(Location location, Runnable runnable, Runnable fireTicksResetCallback) {
    if(getVehicle() != null) {
      exitVehicle();
      getVehicle().remove();
    }

    switch (race.getType()) {
      case PIG:
        spawnPig(location);
        break;

      case MINECART:
        spawnMinecart(location);
        break;

      case HORSE:
        spawnHorse(location, horse);
        break;

      case BOAT:
        spawnBoat(location);
        break;
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      player.teleport(location);
      // important to set this after teleporting away from a potential source of fire.
      // 2 ticks looks like the minimum amount of ticks needed to wait after setting it to zero...
      Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
        player.setFireTicks(0);
        if(fireTicksResetCallback != null) {
          fireTicksResetCallback.run();
        }
      }, 2);
      enterVehicle();
      if(runnable != null) {
        runnable.run();
      }
    }, 1L);
  }

  public void respawn(RespawnType type, Runnable runnable, Runnable fireTicksResetCallback) {
    if(type == null) {
      throw new NullPointerException();
    }

    Location loc;
    if(type == RespawnType.FROM_START || currentCheckpoint == null) {
      loc = startLocation;
    } else {
      loc = currentCheckpoint.getLocation().getWorld().getHighestBlockAt(
        currentCheckpoint.getLocation()
      ).getLocation();
    }

    if (player.isSleeping()) {
      // 2 seconds of very high resistance
      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 255, false, false, false));
      player.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 1, 1, false, false, false));
    }

    player.setFallDistance(0);

    switch (race.getType()) {
      case PLAYER:
      case ELYTRA:
        player.teleport(loc);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
          player.setFireTicks(0);
          if(fireTicksResetCallback != null) {
            fireTicksResetCallback.run();
          }
        }, 2);
        break;
      case MINECART:
      case BOAT:
      case HORSE:
      case PIG:
        respawnInVehicle(loc, runnable, fireTicksResetCallback);
        break;
    }
  }

  private void spawnPig(Location location) {
    pig = (Pig) startLocation.getWorld().spawnEntity(location, EntityType.PIG);
    pig.setInvulnerable(true);
    pig.setAI(false);
    pig.setSaddle(true);
    pig.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getPigSpeed());
  }

  private void spawnHorse(Location location, Horse oldHorse) {
    horse = (Horse) startLocation.getWorld().spawnEntity(location, EntityType.HORSE);
    horse.setInvulnerable(true);
    horse.setAI(false);
    horse.setTamed(true);
    horse.setOwner(player);
    horse.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
    horse.setJumpStrength(race.getHorseJumpStrength());
    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getHorseSpeed());

    if(oldHorse != null) {
      horse.setColor(oldHorse.getColor());
      horse.setStyle(oldHorse.getStyle());
      horse.setAge(oldHorse.getAge());
    }
  }

  public void freezeHorse() {
    horse.setJumpStrength(0);
    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
  }

  private void unfreezeHorse() {
    horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getHorseSpeed());
    horse.setJumpStrength(race.getHorseJumpStrength());
  }

  private void spawnBoat(Location location) {
    boat = (Boat) startLocation.getWorld().spawnEntity(location, EntityType.BOAT);
    boat.setInvulnerable(true);
    boat.setWoodType(TreeSpecies.GENERIC);
  }

  private void spawnMinecart(Location location) {
    minecart = (Minecart) startLocation.getWorld().spawnEntity(location, EntityType.MINECART);
    minecart.setInvulnerable(true);
  }

  void setStartPoint(RaceStartPoint startPoint) {
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
    if(isRestored) {
      return;
    }

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
    player.setAllowFlight(allowFlight);
    bossBar.removeAll();
    bossBar = null;
    if(getVehicle() != null) {
      exitVehicle();
      getVehicle().remove();
    }
    pig = null;
    horse = null;
    boat = null;
    minecart = null;

    isRestored = true;
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

  public boolean isRestored() {
    return isRestored;
  }
}
