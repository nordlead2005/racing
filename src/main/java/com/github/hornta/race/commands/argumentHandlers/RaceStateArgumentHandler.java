package com.github.hornta.race.commands.argumentHandlers;

import com.github.hornta.carbon.ValidationResult;
import com.github.hornta.carbon.completers.IArgumentHandler;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class RaceStateArgumentHandler implements IArgumentHandler {

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return Arrays.stream(RaceState.values())
      .map(RaceState::name)
      .map((String s) -> s.toLowerCase(Locale.ENGLISH))
      .filter(state -> state.startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("state", result.getValue());
    MessageManager.setValue("states", Arrays.stream(RaceState.values()).map(RaceState::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.STATE_NOT_FOUND);
  }
}
