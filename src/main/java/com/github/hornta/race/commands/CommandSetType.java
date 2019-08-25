package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandSetType extends RacingCommand implements ICommandHandler {
  public CommandSetType(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);

    if(race.getState() != RaceState.UNDER_CONSTRUCTION) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.EDIT_NO_EDIT_MODE);
      return;
    }

    if(race.getType() == RaceType.fromString(args[1])) {
      MessageManager.setValue("type", race.getType().toString());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_TYPE_NOCHANGE);
      return;
    }

    RaceType oldType = race.getType();
    race.setType(RaceType.fromString(args[1]));

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("old_type", oldType.toString());
      MessageManager.setValue("new_type", race.getType().toString());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_TYPE_SUCCESS);
    });
  }
}

