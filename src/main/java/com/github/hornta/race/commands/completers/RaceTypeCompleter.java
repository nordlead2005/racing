package com.github.hornta.race.commands.completers;

import com.github.hornta.BaseTabCompleter;
import com.github.hornta.race.enums.RaceType;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RaceTypeCompleter implements BaseTabCompleter {

  @Override
  public List<String> getItems(CommandSender sender, String[] arguments) {
    return Arrays.stream(RaceType.values())
      .map(RaceType::name)
      .filter(type -> type.toLowerCase(Locale.ENGLISH).startsWith(arguments[0].toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toList());
  }
}
