package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.ValidationResult;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RaceStateValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender commandSender, String argument, String[] prevArgs) {
    return RaceState.fromString(argument) != null;
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("state", result.getValue());
    MessageManager.setValue("states", Arrays.stream(RaceState.values()).map(RaceState::name).collect(Collectors.joining(", ")));
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.STATE_NOT_FOUND);
  }
}
