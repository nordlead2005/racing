package com.github.hornta.race.commands.argumentHandlers;

import com.github.hornta.ValidationResult;
import com.github.hornta.completers.IArgumentHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class RaceArgumentHandler implements IArgumentHandler {
  private RacingManager racingManager;
  private boolean shouldExist;

  public RaceArgumentHandler(RacingManager racingManager, boolean shouldExist) {
    this.racingManager = racingManager;
    this.shouldExist = shouldExist;
  }

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return racingManager
      .getRaces()
      .stream()
      .filter(race -> race.getName().toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .map(Race::getName)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public boolean test(Set<String> items, String argument) {
    if(shouldExist) {
      return items.contains(argument);
    } else {
      return !items.contains(argument);
    }
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageKey message;
    if(this.shouldExist) {
      message = MessageKey.RACE_NOT_FOUND;
    } else {
      message = MessageKey.RACE_ALREADY_EXIST;
    }
    MessageManager.setValue("race_name", result.getValue());
    MessageManager.sendMessage(result.getCommandSender(), message);
  }
}
