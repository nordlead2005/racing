package com.github.hornta.race.config;

import com.github.hornta.race.api.StorageType;
import com.github.hornta.race.enums.RespawnType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

public enum ConfigKey {
  LANGUAGE("language", ConfigType.STRING, "english"),
  // https://www.loc.gov/standards/iso639-2/php/code_list.php
  LOCALE("locale", ConfigType.STRING, "en", Locale::new),
  SONGS_DIRECTORY("songs_directory", ConfigType.STRING, "songs"),
  STORAGE("storage.current", ConfigType.STRING, StorageType.FILE, StorageType::valueOf),
  FILE_RACE_DIRECTORY("storage.file.directory", ConfigType.STRING, "races"),
  RACE_PREPARE_TIME("prepare_time", ConfigType.INTEGER, 300),
  RACE_ANNOUNCE_INTERVALS("race_announce_intervals", ConfigType.LIST, Arrays.asList(180, 60, 30)),

  RESPAWN_PLAYER_DEATH("respawn.player.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf),
  RESPAWN_PLAYER_INTERACT("respawn.player.interact", ConfigType.STRING, RespawnType.NONE, RespawnType::valueOf),

  RESPAWN_ELYTRA_DEATH("respawn.elytra.death", ConfigType.STRING, RespawnType.FROM_START, RespawnType::valueOf),
  RESPAWN_ELYTRA_INTERACT("respawn.elytra.interact", ConfigType.STRING, RespawnType.FROM_START, RespawnType::valueOf),

  RESPAWN_PIG_DEATH("respawn.pig.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf),
  RESPAWN_PIG_INTERACT("respawn.pig.interact", ConfigType.STRING, RespawnType.NONE, RespawnType::valueOf),

  RESPAWN_HORSE_DEATH("respawn.horse.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf),
  RESPAWN_HORSE_INTERACT("respawn.horse.interact", ConfigType.STRING, RespawnType.NONE, RespawnType::valueOf),

  RESPAWN_BOAT_DEATH("respawn.boat.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf),
  RESPAWN_BOAT_INTERACT("respawn.boat.interact", ConfigType.STRING, RespawnType.NONE, RespawnType::valueOf),

  RESPAWN_MINECART_DEATH("respawn.minecart.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf),
  RESPAWN_MINECART_INTERACT("respawn.minecart.interact", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, RespawnType::valueOf);

  private static final Set<String> configPaths = new HashSet<>();

  static {
    for (ConfigKey key : ConfigKey.values()) {
      if (key.getPath() == null || key.getPath().isEmpty()) {
        throw new Error("A config path can't be null or empty.");
      }

      if(key.getType() == null) {
        throw new Error("A config type can't be null");
      }

      for (char character : key.name().toCharArray()) {
        if (Character.getType(character) == Character.LOWERCASE_LETTER) {
          throw new Error("All characters in a config key must be uppercase");
        }
      }

      for (char character : key.getPath().toCharArray()) {
        if (Character.getType(character) == Character.UPPERCASE_LETTER) {
          throw new Error("All character in a config path must be lowercase");
        }
      }

      if (configPaths.contains(key.getPath())) {
        throw new Error("Duplicate identifier `" + key.getPath() + "` found in ConfigKey");
      }

      configPaths.add(key.getPath());
    }
  }

  private String path;
  private ConfigType type;
  private Object defaultValue;
  private Function<String, Object> converter;

  ConfigKey(String path, ConfigType type, Object defaultValue) {
    this.path = path;
    this.type = type;
    this.defaultValue = defaultValue;
  }

  ConfigKey(String path, ConfigType type, Object defaultValue, Function<String, Object> converter) {
    this.path = path;
    this.type = type;
    this.defaultValue = defaultValue;
    this.converter = converter;
  }

  public static boolean hasPath(String path) {
    return configPaths.contains(path);
  }

  public ConfigType getType() {
    return type;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public String getPath() {
    return path;
  }

  public boolean isExpectedType(ConfigurationSection configurationSection) {
    boolean isExpectedType = false;

    switch (type) {
      case SET:
        isExpectedType = configurationSection.isSet(path);
        break;
      case LIST:
        isExpectedType = configurationSection.isList(path);
        break;
      case LONG:
        isExpectedType = configurationSection.isLong(path);
        break;
      case COLOR:
        isExpectedType = configurationSection.isColor(path);
        break;
      case DOUBLE:
        isExpectedType = configurationSection.isDouble(path);
        break;
      case STRING:
        isExpectedType = configurationSection.isString(path);
        break;
      case VECTOR:
        isExpectedType = configurationSection.isVector(path);
        break;
      case BOOLEAN:
        isExpectedType = configurationSection.isBoolean(path);
        break;
      case INTEGER:
        isExpectedType = configurationSection.isInt(path);
        break;
      case ITEM_STACK:
        isExpectedType = configurationSection.isItemStack(path);
        break;
      case OFFLINE_PLAYER:
        isExpectedType = configurationSection.isOfflinePlayer(path);
        break;
      default:
    }

    return isExpectedType;
  }

  public Function<String, Object> getConverter() {
    return converter;
  }
}
