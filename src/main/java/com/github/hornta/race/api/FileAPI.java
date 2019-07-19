package com.github.hornta.race.api;

import com.github.hornta.race.Racing;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.enums.RaceVersion;
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
  private static final String ID_FIELD = "id";
  private static final String VERSION_FIELD = "version";
  private static final String NAME_FIELD = "name";
  private static final String STATE_FIELD = "state";
  private static final String TYPE_FIELD = "type";
  private static final String SONG_FIELD = "song";
  private static final String CREATED_AT_FIELD = "created_at";
  private static final String FIELD_ENTRY_FEE = "entry_fee";
  private ExecutorService fileService = Executors.newSingleThreadExecutor();
  private File racesDirectory;
  private MigrationManager migrationManager = new MigrationManager();

  public FileAPI(Plugin plugin) {
    racesDirectory = new File(plugin.getDataFolder(), RaceConfiguration.getValue(ConfigKey.FILE_RACE_DIRECTORY));
    migrationManager.addMigration(new EntryFeeMigration());
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
            migrationManager.migrate(yaml);
            try {
              yaml.save(file);
            } catch (IOException e) {
              Racing.getInstance().getLogger().log(Level.SEVERE, "Failed to save race");
            }
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
    yaml.set(ID_FIELD, race.getId().toString());
    yaml.set(VERSION_FIELD, Racing.getInstance().getDescription().getVersion());
    yaml.set(NAME_FIELD, race.getName());
    yaml.set(STATE_FIELD, race.getState().name());
    yaml.set(TYPE_FIELD, race.getType().name());
    yaml.set(SONG_FIELD, race.getSong());
    yaml.set(FIELD_ENTRY_FEE, race.getEntryFee());
    yaml.set(CREATED_AT_FIELD, race.getCreatedAt().getEpochSecond());
    yaml.set("spawn.x", race.getSpawn().getX());
    yaml.set("spawn.y", race.getSpawn().getY());
    yaml.set("spawn.z", race.getSpawn().getZ());
    yaml.set("spawn.pitch", race.getSpawn().getPitch());
    yaml.set("spawn.yaw", race.getSpawn().getYaw());
    yaml.set("spawn.world", race.getSpawn().getWorld().getName());
    writeCheckpoints(race.getCheckpoints(), yaml);
    writeStartPoints(race.getStartPoints(), yaml);

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
      yaml.set("state", race.getState().name());
      yaml.set("type", race.getType().name());
      yaml.set("song", race.getSong());
      yaml.set("spawn.x", race.getSpawn().getX());
      yaml.set("spawn.y", race.getSpawn().getY());
      yaml.set("spawn.z", race.getSpawn().getZ());
      yaml.set("spawn.pitch", race.getSpawn().getPitch());
      yaml.set("spawn.yaw", race.getSpawn().getYaw());
      yaml.set("spawn.world", race.getSpawn().getWorld().getName());

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
  public void addCheckpoint(UUID raceId, RaceCheckpoint checkpoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, raceId + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceCheckpoint> checkpoints = parseCheckpoints(yaml);

      boolean hasPosition = checkpoints.stream().anyMatch((RaceCheckpoint p) -> p.getPosition() == checkpoint.getPosition());

      if(hasPosition) {
        return false;
      }

      checkpoints.add(checkpoint);
      writeCheckpoints(checkpoints, yaml);

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
  public void deleteCheckpoint(UUID raceId, RaceCheckpoint checkpoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, raceId + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceCheckpoint> checkpoints = parseCheckpoints(yaml)
        .stream()
        .filter((RaceCheckpoint point) -> !point.getId().equals(checkpoint.getId()))
        .peek((RaceCheckpoint point) -> {
          if(point.getPosition() > checkpoint.getPosition()) {
            point.setPosition(point.getPosition() - 1);
          }
        })
        .collect(Collectors.toList());

      writeCheckpoints(checkpoints, yaml);

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
  public void addStartPoint(UUID raceId, RaceStartPoint startPoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, raceId + ".yml");

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
  public void deleteStartPoint(UUID raceId, RaceStartPoint startPoint, Consumer<Boolean> callback) {
    File raceFile = new File(racesDirectory, raceId + ".yml");

    CompletableFuture.supplyAsync(() -> {
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(raceFile);

      List<RaceStartPoint> filteredStartPoints = parseStartPoints(yaml)
        .stream()
        .filter((RaceStartPoint point) -> !point.getId().equals(startPoint.getId()))
        .peek((RaceStartPoint point) -> {
          if(point.getPosition() > startPoint.getPosition()) {
            point.setPosition(point.getPosition() - 1);
          }
        })
        .collect(Collectors.toList());

      writeStartPoints(filteredStartPoints, yaml);

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
    String idString = yaml.getString(ID_FIELD);

    if(idString == null) {
      throw new ParseRaceException("`" + ID_FIELD + "` is missing from race");
    }

    RaceVersion version = RaceVersion.fromString(yaml.getString("version"));
    if(version == null) {
      throw new ParseRaceException("`" + VERSION_FIELD + "` is missing from race");
    }

    UUID id;
    try {
      id = UUID.fromString(idString);
    } catch (IllegalArgumentException ex) {
      throw new ParseRaceException("Couldn't convert id to UUID");
    }

    Location spawn;
    try {
      spawn = parseLocation(yaml, "spawn");
    } catch (ParseYamlLocationException ex) {
      throw new ParseRaceException("Couldn't parse spawn: " + ex.getMessage());
    }

    String name = yaml.getString(NAME_FIELD);
    if(name == null) {
      throw new ParseRaceException("`" + NAME_FIELD + "` is missing from race");
    }

    Instant createdAt;
    try {
      createdAt = Instant.ofEpochSecond(yaml.getLong(CREATED_AT_FIELD));
    } catch (DateTimeException ex) {
      throw new ParseRaceException("`" + CREATED_AT_FIELD + "` is invalid");
    }

    RaceType type;
    try {
      type = RaceType.valueOf(yaml.getString(TYPE_FIELD));
    } catch (IllegalArgumentException ex) {
      throw new ParseRaceException("`" + TYPE_FIELD + "` is invalid");
    }

    RaceState state;
    try {
      state = RaceState.valueOf(yaml.getString(STATE_FIELD));
    } catch (IllegalArgumentException ex) {
      throw new ParseRaceException("`" + STATE_FIELD + "` is invalid");
    }

    if(!yaml.contains(FIELD_ENTRY_FEE) || (!yaml.isDouble(FIELD_ENTRY_FEE) && !yaml.isInt(FIELD_ENTRY_FEE))) {
      throw new ParseRaceException("Missing field `" + FIELD_ENTRY_FEE + "`");
    }

    double entryFee;
    if(yaml.isDouble(FIELD_ENTRY_FEE)) {
      entryFee = yaml.getDouble(FIELD_ENTRY_FEE);
    } else {
      entryFee = yaml.getInt(FIELD_ENTRY_FEE);
    }

    List<RaceCheckpoint> checkpoints = parseCheckpoints(yaml);
    List<RaceStartPoint> startPoints = parseStartPoints(yaml);

    String song = yaml.getString(SONG_FIELD, null);

    return new Race(id, version, name, spawn, state, createdAt, checkpoints, startPoints, type, song, entryFee);
  }

  private List<RaceCheckpoint> parseCheckpoints(YamlConfiguration yaml) {
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

  private void writeCheckpoints(List<RaceCheckpoint> checkpoints, YamlConfiguration yaml) {
    yaml.set(CHECKPOINTS_LOCATION, null);
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

  private void writeStartPoints(List<RaceStartPoint> startPoints, YamlConfiguration yaml) {
    yaml.set(START_POINTS_LOCATION, null);
    if(!yaml.contains(START_POINTS_LOCATION)) {
      yaml.createSection(START_POINTS_LOCATION);
    }

    for(RaceStartPoint startPoint : startPoints) {
      String key = START_POINTS_LOCATION + "." + startPoint.getId();
      yaml.set(key + ".position", startPoint.getPosition());
      yaml.set(key + ".location", startPoint.getPosition());
      writeLocation(startPoint.getLocation(), yaml, key + ".location");
    }
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
