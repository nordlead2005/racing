package com.github.hornta.race.commands.argumentHandlers;

import com.github.hornta.carbon.ValidationResult;
import com.github.hornta.carbon.completers.IArgumentHandler;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class RaceTypeArgumentHandler implements IArgumentHandler {

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return Arrays.stream(RaceType.values())
      .map(RaceType::name)
      .map((String s) -> s.toLowerCase(Locale.ENGLISH))
      .filter(type -> type.startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    return items.contains(argument.toLowerCase(Locale.ENGLISH));
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("type", result.getValue());
    MessageManager.setValue("types", Arrays.stream(RaceType.values()).map(RaceType::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.TYPE_NOT_FOUND);
  }
}
