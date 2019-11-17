package com.github.hornta.race.commands.argumentHandlers;

import com.github.hornta.carbon.ValidationResult;
import com.github.hornta.carbon.completers.IArgumentHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class StartPointArgumentHandler implements IArgumentHandler {
  private RacingManager racingManager;
  private boolean shouldExist;

  public StartPointArgumentHandler(RacingManager racingManager, boolean shouldExist) {
    this.racingManager = racingManager;
    this.shouldExist = shouldExist;
  }

  @Override
  public Set<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return racingManager.getRace(prevArgs[0]).getStartPoints().stream()
      .filter(point -> String.valueOf(point.getPosition()).toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .map(p -> String.valueOf(p.getPosition()))
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
    MessageKey key;
    if(this.shouldExist) {
      key = MessageKey.STARTPOINT_NOT_FOUND;
    } else {
      key = MessageKey.STARTPOINT_ALREADY_EXIST;
    }
    MessageManager.setValue("race_name", result.getPrevArgs()[0]);
    MessageManager.setValue("position", result.getValue());
    MessageManager.sendMessage(result.getCommandSender(), key);
  }
}
