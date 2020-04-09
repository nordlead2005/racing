package com.github.hornta.race.api;

import com.github.hornta.race.Racing;
import com.github.hornta.race.enums.RaceVersion;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MigrationManager {
  private List<IFileMigration> migrations = new ArrayList<>();

  public void addMigration(IFileMigration migration) {

    if(migration.from().equals(migration.to())) {
      throw new IllegalArgumentException("Migration from() and to() must return different values");
    }

    if(migration.from().isGreater(migration.to())) {
      throw new IllegalArgumentException("Migration from() must not be greater than to()");
    }

    for(int i = 0; i < migrations.size(); ++i) {
      if(migrations.get(i).from().equals(migration.from())) {
        throw new IllegalArgumentException("There is already a migration with the same from()");
      }
      if(migrations.get(i).to().equals(migration.to())) {
        throw new IllegalArgumentException("There is already a migration with the same to()");
      }
    }

    migrations.add(migration);
  }

  public void migrate(YamlConfiguration yaml) {
    RaceVersion version;

    try {
      version = RaceVersion.fromString(yaml.getString("version"));
    } catch (IllegalArgumentException e) {
      throw new ParseRaceException("Couldn't find version");
    }

    RaceVersion currentVersion = RaceVersion.getLast();

    if(version.isGreater(currentVersion)) {
      throw new ParseRaceException("Race version greater than plugin race version not supported.");
    }

    if(version == currentVersion) {
      return;
    }

    for(IFileMigration migration : migrations) {
      RaceVersion fromVersion = RaceVersion.fromString(yaml.getString("version"));
      if(migration.from().equals(fromVersion)) {
        migration.migrate(yaml);
        yaml.set("version", migration.to().toString());
        Racing.logger().info("Migrate race from version " + fromVersion.toString() + " to " + migration.to().toString());
      }
    }
  }
}
