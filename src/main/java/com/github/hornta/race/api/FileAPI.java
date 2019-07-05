package com.github.hornta.race.api;

import com.github.hornta.race.Racing;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.enums.RacingType;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FileAPI implements RacingAPI {
  private static final String CHECKPOINTS_LOCATION = "checkpoints";
  private static final String START_POINTS_LOCATION = "startPoints";
  private ExecutorService fileService = Executors.newSingleThreadExecutor();
  private File racesDirectory;

  public FileAPI(Plugin plugin) {
    racesDirectory = new File(plugin.getDataFolder(), RaceConfiguration.getValue(ConfigKey.FILE_RACE_DIRECTORY));
  }

  @Override
  public void fetchAllRaces(Consumer<List<Race>> callback) {
    CompletableFuture.supplyAsync(() -> {
      List<Race> races = new ArrayList<>();
      File[] files = racesDirectory.listFiles();

      if (files == null) {
        callback.accept(races);
        return races;
      }

      for (File file : files) {
        if (file.isFile()) {
          YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

          try {
            Race race = parseRace(yaml);
            races.add(race);
          } catch (ParseRaceException ex) {
            Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
          }
        }
      }

      return races;
    }).thenAccept(callback);
  }

  @Override
  public void createRace(Race race, Consumer<Boolean> callback) {
    File file = new File(racesDirectory, race.getId() + ".yml");

    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("id", race.getId().toString());
    yaml.set("version", Racing.getInstance().getDescription().getVersion());
    yaml.set("name", race.getName());
    yaml.set("spawn.x", race.getSpawn().getX());
    yaml.set("spawn.y", race.getSpawn().getY());
    yaml.set("spawn.z", race.getSpawn().getZ());
    yaml.set("spawn.pitch", race.getSpawn().getPitch());
    yaml.set("spawn.yaw", race.getSpawn().getYaw());
    yaml.set("spawn.world", race.getSpawn().getWorld().getName());
    yaml.set("created_at", race.getCreatedAt().getEpochSecond());
    yaml.set("type", race.getType().name());
    yaml.set("song", race.getSong());
    writeRaceCheckpoints(race.getCheckpoints(), yaml);
    writeStartPoints(race.getStartPoints(), yaml);
    yaml.set("is_enabled", race.isEnabled());
    yaml.set("is_editing", race.isEnabled());

    CompletableFuture.supplyAsync(() -> {
      try {
        yaml.save(file);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;
    }, fileService).thenAccept(callback);
  }

  @Override
  public void deleteRace(Race race, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      boolean success = false;
      try {
        Files.delete(raceFile.toPath());
        success = true;
      } catch (NoSuchFileException ex) {
        Racing.logger().log(Level.WARNING, "Failed to delete race file. File `" + raceFile.getName() + "` wasn't found.", ex);
      } catch (DirectoryNotEmptyException ex) {
        Racing.logger().log(Level.SEVERE, "Failed to delete race file. Expected a file but tried to delete a folder", ex);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
      }

      return success;
    }, fileService).thenAccept(callback);
  }

  @Override
  public void updateRace(Race race, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);
      yaml.set("name", race.getName());
      yaml.set("spawn.x", race.getSpawn().getX());
      yaml.set("spawn.y", race.getSpawn().getY());
      yaml.set("spawn.z", race.getSpawn().getZ());
      yaml.set("spawn.pitch", race.getSpawn().getPitch());
      yaml.set("spawn.yaw", race.getSpawn().getYaw());
      yaml.set("spawn.world", race.getSpawn().getWorld().getName());
      yaml.set("type", race.getType().name());
      yaml.set("is_enabled", race.isEnabled());
      yaml.set("is_editing", race.isEditing());

      try {
        yaml.save(raceFile);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;

    }).thenAccept(callback);
  }

  @Override
  public void addStartPoint(Race race, RaceCheckpoint checkpoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceCheckpoint> checkpoints = parseRaceCheckpoints(yaml);

      boolean hasPosition = checkpoints.stream().anyMatch((RaceCheckpoint p) -> p.getPosition() == checkpoint.getPosition());

      if(hasPosition) {
        return false;
      }

      checkpoints.add(checkpoint);
      writeRaceCheckpoints(checkpoints, yaml);

      try {
        yaml.save(raceFile);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;
    }, fileService).thenAccept(callback);
  }

  @Override
  public void deletePoint(Race race, RaceCheckpoint checkpoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceCheckpoint> checkpoints = parseRaceCheckpoints(yaml)
        .stream()
        .filter((RaceCheckpoint point) -> !point.getId().equals(checkpoint.getId()))
        .collect(Collectors.toList());

      yaml.set(CHECKPOINTS_LOCATION, checkpoints);

      try {
        yaml.save(raceFile);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;
    }).thenAccept(callback);
  }

  @Override
  public void addRaceStart(Race race, RaceStartPoint startPoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceStartPoint> startPoints = parseStartPoints(yaml);

      boolean hasPosition = startPoints.stream().anyMatch((RaceStartPoint p) -> p.getPosition() == startPoint.getPosition());

      if(hasPosition) {
        return false;
      }

      startPoints.add(startPoint);
      writeStartPoints(startPoints, yaml);

      try {
        yaml.save(raceFile);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;
    }, fileService).thenAccept(callback);
  }

  @Override
  public void deleteStartPoint(Race race, RaceStartPoint startPoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, race.getId() + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);
      List<RaceStartPoint> filteredCheckpoints = parseStartPoints(yaml)
        .stream()
        .filter((RaceStartPoint point) -> !point.getId().equals(startPoint.getId()))
        .collect(Collectors.toList());
      yaml.set(START_POINTS_LOCATION, filteredCheckpoints);

      try {
        yaml.save(raceFile);
      } catch (IOException ex) {
        Racing.logger().log(Level.SEVERE, ex.getMessage(), ex);
        return false;
      }

      return true;
    }).thenAccept(callback);
  }

  private Race parseRace(YamlConfiguration yaml) throws ParseRaceException {
    String idString = yaml.getString("id");

    if(idString == null) {
      throw new ParseRaceException("`id` is missing from race");
    }

    UUID id;
    try {
      id = UUID.fromString(idString);
    } catch (IllegalArgumentException ex) {
      throw new ParseRaceException("Couldn't convert id to UUID");
    }

    String version = yaml.getString("version");
    if(version == null) {
      throw new ParseRaceException("Couldn't find version");
    }

    Location spawn;
    try {
      spawn = parseLocation(yaml, "spawn");
    } catch (ParseYamlLocationException ex) {
      throw new ParseRaceException("Couldn't parse spawn: " + ex.getMessage());
    }

    String name = yaml.getString("name");
    if(name == null) {
      throw new ParseRaceException("`name` is missing from race");
    }

    Instant createdAt;
    try {
      createdAt = Instant.ofEpochSecond(yaml.getLong("created_at"));
    } catch (DateTimeException ex) {
      throw new ParseRaceException("`created_at` is invalid");
    }

    RacingType type;
    try {
      type = RacingType.valueOf(yaml.getString("type"));
    } catch (IllegalArgumentException ex) {
      throw new ParseRaceException("`type` is invalid");
    }

    if(!yaml.isBoolean("is_editing")) {
      throw new ParseRaceException("`is_editing` flag is missing from race");
    }

    if(!yaml.isBoolean("is_enabled")) {
      throw new ParseRaceException("`is_enabled` flag is missing from race");
    }

    boolean isEditing = yaml.getBoolean("is_editing");
    boolean isEnabled = yaml.getBoolean("is_enabled");

    List<RaceCheckpoint> checkpoints = parseRaceCheckpoints(yaml);
    List<RaceStartPoint> startPoints = parseStartPoints(yaml);

    String song = yaml.getString("song", null);

    return new Race(id, version, name, spawn, isEnabled, isEditing, createdAt, checkpoints, startPoints, type, song);
  }

  private List<RaceCheckpoint> parseRaceCheckpoints(YamlConfiguration yaml) {
    List<RaceCheckpoint> checkpoints = new ArrayList<>();
    ConfigurationSection section = yaml.getConfigurationSection(CHECKPOINTS_LOCATION);

    if(section == null) {
      Racing.logger().log(Level.SEVERE, "Couldn't find checkpoints section");
      return checkpoints;
    }

    for(String key : section.getKeys(false)) {
      UUID id;
      try {
        id = UUID.fromString(key);
      } catch (IllegalArgumentException ex) {
        Racing.logger().log(Level.SEVERE, "Couldn't convert key to UUID", ex);
        continue;
      }

      String positionPath = key + ".position";
      if(!section.isInt(positionPath)) {
        Racing.logger().log(Level.SEVERE, "`position` must be of type integer");
        continue;
      }

      int position = section.getInt(positionPath);

      Location location;
      try {
        location = parseLocation(section, key + ".location");
      }  catch (ParseYamlLocationException ex) {
        Racing.logger().log(Level.SEVERE, "Couldn't parse location: " + ex.getMessage(), ex);
        continue;
      }

      String radiusPath = key + ".radius";
      if(!section.isInt(radiusPath)) {
        Racing.logger().log(Level.SEVERE, "`radius` must be of type integer");
        continue;
      }

      checkpoints.add(new RaceCheckpoint(id, position, location, section.getInt(radiusPath)));
    }

    return checkpoints;
  }

  private boolean writeRaceCheckpoints(List<RaceCheckpoint> checkpoints, YamlConfiguration yaml) {
    if(!yaml.contains(CHECKPOINTS_LOCATION)) {
      yaml.createSection(CHECKPOINTS_LOCATION);
    }
    for(RaceCheckpoint checkpoint : checkpoints) {
      String key = CHECKPOINTS_LOCATION + "." + checkpoint.getId();
      yaml.set(key + ".position", checkpoint.getPosition());
      yaml.set(key + ".location", checkpoint.getPosition());
      writeLocation(checkpoint.getLocation(), yaml, key + ".location");
      yaml.set(key + ".radius", checkpoint.getRadius());
    }
    return true;
  }

  private List<RaceStartPoint> parseStartPoints(YamlConfiguration yaml) {
    List<RaceStartPoint> startPoints = new ArrayList<>();
    ConfigurationSection section = yaml.getConfigurationSection(START_POINTS_LOCATION);

    if(section == null) {
      Racing.logger().log(Level.SEVERE, "Couldn't find startpoints section");
      return startPoints;
    }

    for(String key : section.getKeys(false)) {
      UUID id;
      try {
        id = UUID.fromString(key);
      } catch (IllegalArgumentException ex) {
        Racing.logger().log(Level.SEVERE, "Couldn't convert key to UUID", ex);
        continue;
      }

      String positionPath = key + ".position";
      if(!section.isInt(positionPath)) {
        Racing.logger().log(Level.SEVERE, "`position` must be of type integer");
        continue;
      }

      int position = section.getInt(positionPath);

      Location location;

      try {
        location = parseLocation(section, key + ".location");
      }  catch (ParseYamlLocationException ex) {
        Racing.logger().log(Level.SEVERE, "Couldn't parse location: " + ex.getMessage(), ex);
        continue;
      }

      startPoints.add(new RaceStartPoint(id, position, location));
    }

    return startPoints;
  }

  private Location parseLocation(ConfigurationSection section, String path) throws ParseYamlLocationException {
    String xPath = path + ".x";
    if(!section.isDouble(xPath)) {
      throw new ParseYamlLocationException("Expected `" + xPath + "` to be an integer");
    }

    String yPath = path + ".y";
    if(!section.isDouble(yPath)) {
      throw new ParseYamlLocationException("Expected `" + yPath + "` to be an integer");
    }

    String zPath = path + ".z";
    if(!section.isDouble(zPath)) {
      throw new ParseYamlLocationException("Expected `" + zPath + "` to be an integer");
    }

    String pitchPath = path + ".pitch";
    if(!section.isDouble(pitchPath)) {
      throw new ParseYamlLocationException("Expected `" + pitchPath + "` to be a double");
    }

    String yawPath = path + ".yaw";
    if(!section.isDouble(yawPath)) {
      throw new ParseYamlLocationException("Expected `" + yawPath + "` to be a double");
    }

    String worldPath = path + ".world";
    String worldName = section.getString(worldPath);
    if(worldName == null) {
      throw new ParseYamlLocationException("Expected `" + worldPath + "` to be a string");
    }

    World world = Bukkit.getWorld(worldName);

    if(world == null) {
      throw new ParseYamlLocationException("Couldn't find world with name `" + section.getString(worldPath) + "`");
    }

    return new Location(
      world,
      section.getDouble(xPath),
      section.getDouble(yPath),
      section.getDouble(zPath),
      (float)section.getDouble(yawPath),
      (float)section.getDouble(pitchPath)
    );
  }

  private boolean writeStartPoints(List<RaceStartPoint> startPoints, YamlConfiguration yaml) {
    if(!yaml.contains(START_POINTS_LOCATION)) {
      yaml.createSection(START_POINTS_LOCATION);
    }
    for(RaceStartPoint startPoint : startPoints) {
      String key = START_POINTS_LOCATION + "." + startPoint.getId();
      yaml.set(key + ".position", startPoint.getPosition());
      yaml.set(key + ".location", startPoint.getPosition());
      writeLocation(startPoint.getLocation(), yaml, key + ".location");
    }
    return true;
  }

  private void writeLocation(Location location, YamlConfiguration yaml, String path) {
    yaml.set(path + ".x", location.getX());
    yaml.set(path + ".y", location.getY());
    yaml.set(path + ".z", location.getZ());
    yaml.set(path + ".pitch", location.getPitch());
    yaml.set(path + ".yaw", location.getYaw());
    yaml.set(path + ".world", location.getWorld().getName());
  }
}
