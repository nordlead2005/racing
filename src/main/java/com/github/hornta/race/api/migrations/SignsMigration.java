package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.IFileMigration;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;

public class SignsMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V4;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V5;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    yamlConfiguration.set("signs", Collections.emptyList());
  }
}
