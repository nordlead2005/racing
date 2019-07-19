package com.github.hornta.race.commands.completers;

import com.github.hornta.BaseTabCompleter;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RaceCompleter implements BaseTabCompleter {
  private RacingManager racingManager;

  public RaceCompleter(RacingManager racingManager) {
    this.racingManager = racingManager;
  }

  @Override
  public List<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return racingManager.getRaces().stream()
      .filter(race -> race.getName().toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .map(Race::getName)
      .collect(Collectors.toList());
  }
}
