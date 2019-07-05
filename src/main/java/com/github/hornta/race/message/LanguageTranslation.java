package com.github.hornta.race.message;

import com.github.hornta.race.Racing;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class LanguageTranslation {
  private File file;
  private Map<MessageKey, String> translations = new EnumMap<>(MessageKey.class);

  LanguageTranslation(File file) {
    this.file = file;
  }

  boolean load() {
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    MessageKey[] messageKeys = MessageKey.values();

    Set<String> validationErrors = new HashSet<>();

    Set<String> yamlKeys = yaml.getKeys(true);
    for(String key : yamlKeys) {
      if(yaml.isConfigurationSection(key)) {
        continue;
      }

      // in case translation contains redundant keys
      if(!MessageKey.hasIdentifier(key)) {
        yaml.set(key, null);
        Racing.logger().log(Level.WARNING, "Deleted unused path `" + key + "` in `" + file.getName() + "`");

        // in case some key is not a string
      } else if (!yaml.isString(key)) {
        validationErrors.add("Expected \"" + key + "\" in \"" + file.getName() + "\" to be of type string.");
      }
    }

    try {
      yaml.save(file);
    } catch (IOException ex) {
      Racing.logger().log(Level.SEVERE, "Failed to save to " + file.getName(), ex);
    }

    // in case translation contains missing keys
    for(MessageKey key : messageKeys) {
      if (yaml.getString(key.getIdentifier()) == null) {
        validationErrors.add("Expected path \"" + key.getIdentifier() + "\" to exist in \"" + file.getName() + "\".");
      }
    }

    if (!validationErrors.isEmpty()) {
      Racing.logger().log(Level.SEVERE, "*** " + file.getName() + " has bad values ***");
      for(String string : validationErrors) {
        Racing.logger().log(Level.SEVERE, "*** " + string + " ***");
      }
      return false;
    }

    for(MessageKey key : messageKeys) {
      translations.put(key, yaml.getString(key.getIdentifier()));
    }

    return true;
  }

  public String getTranslation(MessageKey key) {
    return translations.get(key);
  }
}
