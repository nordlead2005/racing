package com.github.hornta.race.config;

import com.github.hornta.race.Racing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class RaceConfiguration {
  private static RaceConfiguration instance;
  private Configuration configuration;

  private RaceConfiguration(Configuration configuration) {
    this.configuration = configuration;

    deleteUnusedValues();
  }

  public static boolean init(JavaPlugin plugin) {
    instance = new RaceConfiguration(plugin.getConfig());
    boolean result = instance.validate();
    plugin.saveConfig();
    return result;
  }

  public static boolean reload(JavaPlugin plugin) {
    plugin.reloadConfig();
    instance.setConfiguration(plugin.getConfig());
    boolean result = instance.validate();
    plugin.saveConfig();
    return result;
  }

  public static <T> T getValue(ConfigKey key) {
    Object obj = instance.getConfiguration().get(key.getPath());

    Function<String, IEnumConfig> converter = key.getConverter();

    if(converter != null) {
      return (T)converter.apply(((String)obj).toUpperCase(Locale.ENGLISH));
    }

    return (T)obj;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  private void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  private void deleteUnusedValues() {
    // try and see if we can delete unused config values
    List<String> keys = new ArrayList<>(configuration.getKeys(true));

    // make sure we reverse the collections so that all leaves ends up first
    Collections.reverse(keys);

    // keys to actually check for being used (leaves)
    Set<String> checkKeys = new HashSet<>();

    for(String key : keys) {
      boolean hasSubstring = false;
      for(String checkKey : checkKeys) {
        if(checkKey.contains(key)) {
          hasSubstring = true;
          break;
        }
      }

      if(hasSubstring) {
        continue;
      }

      checkKeys.add(key);
    }

    for(String path : checkKeys) {
      tryDeletePathRecursively(path);
    }
  }

  private void tryDeletePathRecursively(String path) {
    if(ConfigKey.hasPath(path)) {
      return;
    }
    configuration.set(path, null);
    Racing.logger().log(Level.WARNING, "Deleted unused path `" + path + "`");
    int separatorIndex = path.lastIndexOf('.');
    if(separatorIndex != -1) {
      tryDeletePathRecursively(path.substring(0, separatorIndex));
    }
  }

  private boolean validate() {
    Set<String> errors = new HashSet<>();

    // store keys and values in order defined in ConfigKey so that when saving new keys they end up in order when saving the config.yml
    Map<String, Object> keyValues = new LinkedHashMap<>();

    boolean save = false;
    for (ConfigKey configKey : ConfigKey.values()) {
      // try and see if we can add missing config values to the config
      if(!configuration.contains(configKey.getPath())) {
        Object value;
        if(configKey.getDefaultValue().getClass().isEnum()) {
          value = ((Enum)configKey.getDefaultValue()).name().toLowerCase(Locale.ENGLISH);
        } else {
          value = configKey.getDefaultValue();
        }
        keyValues.put(configKey.getPath(), value);
        save = true;
        Racing.logger().log(Level.INFO, "Added missing property `" + configKey.getPath() + "` with value `" + value + "`");
        continue;
      }

      keyValues.put(configKey.getPath(), configuration.get(configKey.getPath()));

      // verify that the type in the config file is of the expected type
      boolean isType = configKey.isExpectedType(configuration);

      if(!isType) {
        errors.add("Expected config path \"" + configKey.getPath() + "\" to be of type \"" + configKey.getType().toString() + "\"");
      }
    }

    if(save) {
      // delete everything currently in the config
      for(String key : configuration.getKeys(true)) {
        configuration.set(key, null);
      }

      for(Map.Entry<String, Object> entry : keyValues.entrySet()) {
        configuration.set(entry.getKey(), entry.getValue());
      }
    }

    if(!errors.isEmpty()) {
      Racing.logger().log(Level.SEVERE, "*** config.yml contains bad values ***");
      errors
        .stream()
        .map((String s) -> "*** " + s + " ***")
        .forEach(Bukkit.getLogger()::severe);
      return false;
    }

    return true;
  }
}
