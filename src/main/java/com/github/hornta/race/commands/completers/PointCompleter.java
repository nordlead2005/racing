package com.github.hornta.race.commands.completers;

import com.github.hornta.BaseTabCompleter;
import com.github.hornta.race.RacingManager;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PointCompleter implements BaseTabCompleter {
  private RacingManager racingManager;

  public PointCompleter(RacingManager racingManager) {
    this.racingManager = racingManager;
  }

  @Override
  public List<String> getItems(CommandSender sender, String[] arguments) {
    return racingManager.getRace(arguments[0]).getCheckpoints().stream()
      .filter(race -> String.valueOf(race.getPosition()).toLowerCase(Locale.ENGLISH).startsWith(arguments[1].toLowerCase(Locale.ENGLISH)))
      .map(cp -> String.valueOf(cp.getPosition()))
      .collect(Collectors.toList());
  }
}
