package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.FileAPI;
import com.github.hornta.race.api.IFileMigration;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public class WalkSpeedMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V2;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V3;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    yamlConfiguration.set(FileAPI.WALK_SPEED_FIELD, 0.2D);
  }
}
