package com.github.hornta.race.commands.completers;

import com.github.hornta.BaseTabCompleter;
import com.github.hornta.race.SongManager;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SongCompleter implements BaseTabCompleter {
  @Override
  public List<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return SongManager.getSongNames().stream()
      .filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toList());
  }
}
