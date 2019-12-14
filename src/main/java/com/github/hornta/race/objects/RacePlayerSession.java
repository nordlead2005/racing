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
  private Entity vehicle;
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
    Racing.debug("New RacePlayerSession\nPlayer: %s\nUUID: %s\nCharged: %f", playerName, playerId, chargedEntryFee);
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

  void startCooldown() {
    Racing.debug("Starting cooldown for RacePlayerSession %s", playerName);
    walkSpeed = player.getWalkSpeed();
    foodLevel = player.getFoodLevel();
    inventory = player.getInventory().getContents();
    potionEffects = new ArrayList<>(player.getActivePotionEffects());
    health = player.getHealth();
    gameMode = player.getGameMode();
    fireTicks = player.getFireTicks();
    allowFlight = player.getAllowFlight();

    Racing.debug("Attempting to set walk speed to %f on %s", 0f, player.getName());
    player.setWalkSpeed(0);
    Racing.debug("Walk speed was set to %f on %s", player.getWalkSpeed(), player.getName());
    Racing.debug("Attempting to set food level to %d on %s", PREVENT_SPRINT_FOOD_LEVEL, player.getName());
    player.setFoodLevel(PREVENT_SPRINT_FOOD_LEVEL);
    Racing.debug("Setting food level to %d on %s", player.getFoodLevel(), player.getName());
    Racing.debug("Attempting to clear inventory of %s", player.getName());
    player.getInventory().clear();
    Racing.debug("Inventory of %s was cleared. Contents: %s", player.getName(), player.getInventory().getContents());

    Racing.debug("Attempting to remove potion effects on %s", player.getName());
    for(PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
      Racing.debug("Removed potion effect %s on %s", effect.getType(), player.getName());
    }

    // prevent players from jumping during the countdown
    int countdown = Racing.getInstance().getConfiguration().get(ConfigKey.COUNTDOWN);
    Racing.debug("Attempting to add jump potion effect on %s", player.getName());
    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, countdown * 20, 128));
    Racing.debug("Potion effect %s added to %s", player.getPotionEffect(PotionEffectType.JUMP), player.getName());

    if(Racing.getInstance().getConfiguration().get(ConfigKey.ADVENTURE_ON_START)) {
      player.setGameMode(GameMode.ADVENTURE);
      Racing.debug("Setting game mode to %s on %s", GameMode.ADVENTURE, player.getName());
    }

    player.closeInventory();
    Racing.debug("Closing inventory of %s", player.getName());

    if(player.isInsideVehicle()) {
      Racing.debug("%s is inside a vehicle. Attempting to eject it...", player.getName());
      isAllowedToExitVehicle = true;
      player.getVehicle().eject();
      isAllowedToExitVehicle = false;
      Racing.debug("Result of ejecting %s from vehicle: %B", player.getName(), player.getVehicle() == null);
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

    Racing.debug("Teleporting %s to start location", player.getName());
    respawn(RespawnType.FROM_START, () -> {
      player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
    }, () -> {
      player.setHealth(MAX_HEALTH);
      Racing.debug("Setting health to %f on %s", MAX_HEALTH, player.getName());
    });

    if(race.getType() == RaceType.HORSE) {
      freezeHorse();
      Racing.debug("Freezed horse movement");
    }

    isRestored = false;
  }

  Entity getVehicle() {
    return vehicle;
  }

  public double getChargedEntryFee() {
    return chargedEntryFee;
  }

  void startRace() {
    player.setWalkSpeed(race.getWalkSpeed());
    player.setFoodLevel(MAX_FOOD_LEVEL);
    player.removePotionEffect(PotionEffectType.JUMP);

    if(vehicle instanceof Pig) {
      player.getInventory().setItemInMainHand(new ItemStack(Material.CARROT_ON_A_STICK, 1));
    } else if(vehicle instanceof Horse) {
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
      location = currentCheckpoint.getLocation();
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
        spawnVehicle(EntityType.PIG, location);
        setupPig();
        break;

      case MINECART:
        spawnVehicle(EntityType.MINECART, location);
        break;

      case HORSE:
        spawnVehicle(EntityType.HORSE, location);
        setupHorse((Horse) vehicle);
        break;

      case BOAT:
        spawnVehicle(EntityType.BOAT, location);
        setupBoat();
        break;
    }

    if(vehicle != null) {
      vehicle.setInvulnerable(true);
      Racing.debug("Making vehicle invulnerable");
    }

    Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(race.getType() == RaceType.BOAT) {
        player.teleport(location.clone().add(0, -0.95, 0));
      } else {
        player.teleport(location);
      }

      // important to set this after teleporting away from a potential source of fire.
      // 2 ticks looks like the minimum amount of ticks needed to wait after setting it to zero...
      Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
        player.setFireTicks(0);
        if (fireTicksResetCallback != null) {
          fireTicksResetCallback.run();
        }
      }, 2);
      enterVehicle();
      if (runnable != null) {
        runnable.run();
      }
    }, 1L);
  }

  public void respawn(RespawnType type, Runnable runnable, Runnable fireTicksResetCallback) {
    Racing.debug("Respawn %s %s", player.getName(), type);
    if(type == null) {
      throw new NullPointerException();
    }

    Location loc;
    if(type == RespawnType.FROM_START || currentCheckpoint == null) {
      loc = startLocation;
    } else {
      loc = currentCheckpoint.getLocation();
    }

    if (player.isSleeping()) {
      Racing.debug("%s was sleeping. Trying to wake it up...", player.getName());
      // 2 seconds of very high resistance
      player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 255, false, false, false));
      player.addPotionEffect(new PotionEffect(PotionEffectType.HARM, 1, 1, false, false, false));
      Racing.debug("Result of waking %s up: %B", player.getName(), player.isSleeping());
    }

    Racing.debug("Attempting to set fall distance to zero on %s to prevent taking damage from falling", player.getName());
    player.setFallDistance(0);
    Racing.debug("Fall distance on %s was set to %f", player.getName(), player.getFallDistance());

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

  private void spawnVehicle(EntityType type, Location location) {
    Racing.debug("Attempting to spawn vehicle of type %s at %s", type, location);
    vehicle = startLocation.getWorld().spawnEntity(location.clone().add(0, -0.95, 0), type);
    Racing.debug("Spawned vehicle at " + vehicle.getLocation());
  }

  private void setupPig() {
    ((Pig)vehicle).setAI(false);
    Racing.debug("Disable pig AI");
    ((Pig)vehicle).setSaddle(true);
    Racing.debug("Giving pig a saddle");
    ((Pig)vehicle).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getPigSpeed());
    Racing.debug("Setting movementspeed on pig to " + race.getPigSpeed());
  }

  private void setupHorse(Horse oldHorse) {
    ((Horse) vehicle).setAI(false);
    Racing.debug("Disable horse AI");
    ((Horse) vehicle).setTamed(true);
    Racing.debug("Set horse to be tamed");
    ((Horse) vehicle).setOwner(player);
    Racing.debug("Set horse owner to " + player.getName());
    ((Horse) vehicle).getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
    Racing.debug("Giving horse a saddle");
    ((Horse) vehicle).setJumpStrength(race.getHorseJumpStrength());
    Racing.debug("Setting horse jump strength to" + race.getHorseJumpStrength());
    ((Horse) vehicle).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getHorseSpeed());
    Racing.debug("Setting horse movement speed to " + race.getHorseSpeed());

    if(oldHorse != null) {
      Racing.debug("Transferring old horse values to new horse");
      Racing.debug("Attempt to set horse color to " + oldHorse.getColor());
      ((Horse) vehicle).setColor(oldHorse.getColor());
      Racing.debug("Horse color set to " + ((Horse) vehicle).getColor());
      Racing.debug("Attempting to set horse style to " + oldHorse.getStyle());
      ((Horse) vehicle).setStyle(oldHorse.getStyle());
      Racing.debug("Horse style set to " + ((Horse) vehicle).getStyle());
      Racing.debug("Attempting to set horse age to " + oldHorse.getAge());
      ((Horse) vehicle).setAge(oldHorse.getAge());
      Racing.debug("Horse age set to " + ((Horse) vehicle).getAge());
    }
  }

  public void freezeHorse() {
    ((Horse) vehicle).setJumpStrength(0);
    ((Horse) vehicle).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
  }

  private void unfreezeHorse() {
    ((Horse) vehicle).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(race.getHorseSpeed());
    ((Horse) vehicle).setJumpStrength(race.getHorseJumpStrength());
  }

  private void setupBoat() {
    ((Boat)vehicle).setWoodType(TreeSpecies.GENERIC);
    Racing.debug("Setting boat type to " + TreeSpecies.GENERIC);
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

    Racing.debug("Restoring %s...", player.getName());

    startLocation = null;
    currentCheckpoint = null;

    // its null when player has finished
    if(nextCheckpoint != null) {
      nextCheckpoint.removePlayer(player);
      nextCheckpoint = null;
    }

    player.setWalkSpeed(walkSpeed);
    Racing.debug("Restored walk speed of %s to %f", player.getName(), walkSpeed);

    Racing.debug("Removing potion effects on %s...", player.getName());
    for(PotionEffect effect : player.getActivePotionEffects()) {
      player.removePotionEffect(effect.getType());
      Racing.debug("Removed potion effect %s on %s", effect.getType(), player.getName());
    }

    player.addPotionEffects(potionEffects);
    Racing.debug("Restored potion effects of %s", player.getName());
    player.setFoodLevel(foodLevel);
    Racing.debug("Restored food level on %s to %d", player.getName(), foodLevel);
    player.getInventory().setContents(inventory);
    Racing.debug("Restored inventory of %s to %s", player.getName(), inventory);
    player.setHealth(health);
    Racing.debug("Restored health of %s to %f", player.getName(), health);
    player.setGameMode(gameMode);
    Racing.debug("Restored game mode of %s to %s", player.getName(), gameMode);
    player.setFireTicks(fireTicks);
    Racing.debug("Restored fire ticks of %s to %d", player.getName(), fireTicks);
    player.setAllowFlight(allowFlight);
    Racing.debug("Restoring allowFlight of %s to %b", player.getName(), allowFlight);
    bossBar.removeAll();
    bossBar = null;
    if(getVehicle() != null) {
      Racing.debug("Attempt to eject %s from vehicle", player.getName());
      exitVehicle();
      getVehicle().remove();
      Racing.debug("%s ejected from vehicle result: %b", player.getName(), player.getVehicle() == null);
    }
    vehicle = null;
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
