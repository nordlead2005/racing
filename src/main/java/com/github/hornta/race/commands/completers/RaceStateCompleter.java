package com.github.hornta.race.commands.completers;

import com.github.hornta.BaseTabCompleter;
import com.github.hornta.race.enums.RaceState;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RaceStateCompleter implements BaseTabCompleter {

  @Override
  public List<String> getItems(CommandSender sender, String[] arguments) {
    return Arrays.stream(RaceState.values())
      .map(RaceState::name)
      .filter(state -> state.toLowerCase(Locale.ENGLISH).startsWith(arguments[0].toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toList());
  }
}
