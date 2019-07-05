package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandEnableRace extends RacingCommand implements ICommandHandler {
  public CommandEnableRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(race.isEditing()) {
      MessageManager.sendMessage(commandSender, MessageKey.ENABLE_RACE_IS_EDITING);
      return;
    }

    if(race.isEnabled()) {
      MessageManager.sendMessage(commandSender, MessageKey.ENABLE_RACE_IS_ENABLED);
      return;
    }

    race.setEnabled(true);

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.ENABLE_RACE_SUCCESS);
    });
  }
}
