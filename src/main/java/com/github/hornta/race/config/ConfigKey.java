package com.github.hornta.race.config;

import com.github.hornta.race.api.StorageType;
import com.github.hornta.race.enums.RespawnType;
import com.github.hornta.race.enums.TeleportAfterRaceWhen;
import net.dv8tion.jda.core.entities.Game;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.function.Function;

public enum ConfigKey {
  LANGUAGE("language", ConfigType.STRING, "english"),
  // https://www.loc.gov/standards/iso639-2/php/code_list.php
  LOCALE("locale", ConfigType.STRING, "en", (Object val) -> new Locale(((String)val).toUpperCase(Locale.ENGLISH))),
  SONGS_DIRECTORY("songs_directory", ConfigType.STRING, "songs"),
  STORAGE("storage.current", ConfigType.STRING, StorageType.FILE, (Object val) -> StorageType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  FILE_RACE_DIRECTORY("storage.file.directory", ConfigType.STRING, "races"),
  RACE_PREPARE_TIME("prepare_time", ConfigType.INTEGER, 300),
  RACE_ANNOUNCE_INTERVALS("race_announce_intervals", ConfigType.LIST, Arrays.asList(180, 60, 30)),

  RESPAWN_PLAYER_DEATH("respawn.player.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_PLAYER_INTERACT("respawn.player.interact", ConfigType.STRING, RespawnType.NONE, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  RESPAWN_ELYTRA_DEATH("respawn.elytra.death", ConfigType.STRING, RespawnType.FROM_START, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_ELYTRA_INTERACT("respawn.elytra.interact", ConfigType.STRING, RespawnType.FROM_START, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  RESPAWN_PIG_DEATH("respawn.pig.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_PIG_INTERACT("respawn.pig.interact", ConfigType.STRING, RespawnType.NONE, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  RESPAWN_HORSE_DEATH("respawn.horse.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_HORSE_INTERACT("respawn.horse.interact", ConfigType.STRING, RespawnType.NONE, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  RESPAWN_BOAT_DEATH("respawn.boat.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_BOAT_INTERACT("respawn.boat.interact", ConfigType.STRING, RespawnType.NONE, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  RESPAWN_MINECART_DEATH("respawn.minecart.death", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),
  RESPAWN_MINECART_INTERACT("respawn.minecart.interact", ConfigType.STRING, RespawnType.FROM_LAST_CHECKPOINT, (Object val) -> RespawnType.valueOf(((String)val).toUpperCase(Locale.ENGLISH))),

  DISCORD_ENABLED("discord.enabled", ConfigType.BOOLEAN, false),
  DISCORD_TOKEN("discord.bot_token", ConfigType.STRING, ""),
  DISCORD_ANNOUNCE_CHANNEL("discord.announce_channel", ConfigType.STRING, ""),

  ADVENTURE_ON_START("adventure_mode_on_start", ConfigType.BOOLEAN, true),
  FRIENDLY_FIRE_COUNTDOWN("friendlyfire.countdown", ConfigType.BOOLEAN, false),
  FRIENDLY_FIRE_STARTED("friendlyfire.started", ConfigType.BOOLEAN, false),
  COLLISION_COUNTDOWN("collision.countdown", ConfigType.BOOLEAN, false),
  COLLISION_STARTED("collision.started", ConfigType.BOOLEAN, false),

  ELYTRA_RESPAWN_ON_GROUND("elytra_respawn_on_ground", ConfigType.BOOLEAN, true),

  BLOCKED_COMMANDS("blocked_commands", ConfigType.LIST, Arrays.asList(
    "spawn",
    "wild",
    "wilderness",
    "rtp",
    "tpa",
    "tpo",
    "tp",
    "tpahere",
    "tpaccept",
    "tpdeny",
    "tpyes",
    "tpno",
    "tppos",
    "warp",
    "home",
    "rc spawn",
    "racing spawn"
  )),
  PREVENT_JOIN_FROM_GAME_MODE("prevent_join_from_game_mode", ConfigType.LIST, Collections.emptyList(), (Object value) -> {
    ArrayList<GameMode> gameModes = new ArrayList<>();
    if(value instanceof ArrayList<?>) {
      for (String gameMode : (ArrayList<String>) value) {
        gameModes.add(GameMode.valueOf(gameMode.toUpperCase(Locale.ENGLISH)));
      }
    }
    return gameModes;
  }),
  START_ON_JOIN_SIGN("start_on_join.sign", ConfigType.BOOLEAN, false),
  START_ON_JOIN_COMMAND("start_on_join.command", ConfigType.BOOLEAN, false),

  TELEPORT_AFTER_RACE_ENABLED("teleport_after_race.enabled", ConfigType.BOOLEAN, false),
  TELEPORT_AFTER_RACE_ENABLED_WHEN("teleport_after_race.when", ConfigType.STRING, TeleportAfterRaceWhen.PARTICIPANT_FINISHES, (Object val) -> TeleportAfterRaceWhen.valueOf(((String)val).toUpperCase(Locale.ENGLISH)));

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
  private Function<Object, Object> converter;

  ConfigKey(String path, ConfigType type, Object defaultValue) {
    this.path = path;
    this.type = type;
    this.defaultValue = defaultValue;
  }

  ConfigKey(String path, ConfigType type, Object defaultValue, Function<Object, Object> converter) {
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

  public Function<Object, Object> getConverter() {
    return converter;
  }
}
