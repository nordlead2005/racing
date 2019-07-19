package com.github.hornta.race.api;

import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

public class EntryFeeMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V1;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V2;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    yamlConfiguration.set("entry_fee", 0D);
  }
}
