package com.github.hornta.race.api;

import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public interface IFileMigration {
  RaceVersion from();
  RaceVersion to();
  void migrate(YamlConfiguration yamlConfiguration);
}
