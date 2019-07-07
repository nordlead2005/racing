package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RaceStateValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    return RaceState.fromString(arguments[0]) != null;
  }

  @Override
  public void whenInvalid(CommandSender commandSender, String[] args) {
    MessageManager.setValue("state", args[0]);
    MessageManager.setValue("states", Arrays.stream(RaceState.values()).map(RaceState::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(commandSender, MessageKey.STATE_NOT_FOUND);
  }
}
