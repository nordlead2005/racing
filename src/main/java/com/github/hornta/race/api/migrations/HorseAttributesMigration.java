package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.IFileMigration;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public class HorseAttributesMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V8;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V9;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    yamlConfiguration.set("horse_speed", 0.225D);
    yamlConfiguration.set("horse_jump_strength", 0.7D);
  }
}
