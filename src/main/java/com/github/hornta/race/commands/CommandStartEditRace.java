package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.events.EditingRaceEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandStartEditRace extends RacingCommand implements ICommandHandler {
  public CommandStartEditRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(race.isEditing()) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_START_EDIT_IS_EDITING);
      return;
    }

    if(race.isEnabled()) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_START_EDIT_IS_ENABLED);
      return;
    }

    race.setEditing(true);

    racingManager.updateRace(race, () -> {
      Bukkit.getPluginManager().callEvent(new EditingRaceEvent(race, true));
      MessageManager.sendMessage(commandSender, MessageKey.RACE_START_EDIT_SUCCESS);
    });
  }
}
