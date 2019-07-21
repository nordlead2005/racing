package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.FileAPI;
import com.github.hornta.race.api.IFileMigration;
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
    yamlConfiguration.set(FileAPI.ENTRY_FEE_FIELD, 0D);
  }
}
