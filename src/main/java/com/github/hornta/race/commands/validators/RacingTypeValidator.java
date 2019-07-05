package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.enums.RacingType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RacingTypeValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    RacingType type = RacingType.fromString(arguments[0]);
    return type != null;
  }

  @Override
  public void whenInvalid(CommandSender commandSender, String[] args) {
    MessageManager.setValue("type", args[0]);
    MessageManager.setValue("types", Arrays.stream(RacingType.values()).map(RacingType::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(commandSender, MessageKey.TYPE_NOT_FOUND);
  }
}
