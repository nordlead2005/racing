package com.github.hornta.race.commands.argumentHandlers;

import com.github.hornta.ValidationResult;
import com.github.hornta.completers.IArgumentHandler;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class SongArgumentHandler implements IArgumentHandler {

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return SongManager.getSongNames().stream()
      .filter(name -> name.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("song_name", result.getValue());
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.SONG_NOT_FOUND);
  }
}
