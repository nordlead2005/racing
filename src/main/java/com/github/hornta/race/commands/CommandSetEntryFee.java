package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandSetEntryFee extends RacingCommand implements ICommandHandler {
  public CommandSetEntryFee(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(race.getState() != RaceState.UNDER_CONSTRUCTION) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.EDIT_NO_EDIT_MODE);
      return;
    }

    race.setEntryFee(Double.parseDouble(args[1]));

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("entry_fee", Racing.getInstance().getEconomy().format(race.getEntryFee()));
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_ENTRYFEE);
    });
  }
}

