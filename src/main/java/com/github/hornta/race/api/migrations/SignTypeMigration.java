package com.github.hornta.race.api.migrations;

import com.github.hornta.race.api.IFileMigration;
import com.github.hornta.race.enums.RaceSignType;
import com.github.hornta.race.enums.RaceVersion;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;

public class SignTypeMigration implements IFileMigration {
  @Override
  public RaceVersion from() {
    return RaceVersion.V13;
  }

  @Override
  public RaceVersion to() {
    return RaceVersion.V14;
  }

  @Override
  public void migrate(YamlConfiguration yamlConfiguration) {
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> entries = (List<Map<String, Object>>)yamlConfiguration.getList("signs");
    for(Map<String, Object> sign : entries) {
      sign.put("type", RaceSignType.JOIN.name());
    }
    yamlConfiguration.set("signs", entries);
  }
}
