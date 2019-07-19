package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.ValidationResult;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RaceTypeValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender commandSender, String argument, String[] prevArgs) {
    return RaceType.fromString(argument) != null;
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("type", result.getValue());
    MessageManager.setValue("types", Arrays.stream(RaceType.values()).map(RaceType::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.TYPE_NOT_FOUND);
  }
}
