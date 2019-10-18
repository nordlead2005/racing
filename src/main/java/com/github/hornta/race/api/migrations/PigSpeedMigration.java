package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.IFileMigration;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public class PigSpeedMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V7;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V8;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    yamlConfiguration.set("pig_speed", 0.25D);
  }
}
