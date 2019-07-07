package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RaceTypeValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    return RaceType.fromString(arguments[0]) != null;
  }

  @Override
  public void whenInvalid(CommandSender commandSender, String[] args) {
    MessageManager.setValue("type", args[0]);
    MessageManager.setValue("types", Arrays.stream(RaceType.values()).map(RaceType::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(commandSender, MessageKey.TYPE_NOT_FOUND);
  }
}
