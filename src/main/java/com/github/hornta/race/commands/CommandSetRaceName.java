package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.events.ChangeRaceNameEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandSetRaceName extends RacingCommand implements ICommandHandler {
  public CommandSetRaceName(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(!race.isEditing()) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.EDIT_NO_EDIT_MODE);
      return;
    }

    String oldName = race.getName();
    race.setName(args[1]);

    racingManager.updateRace(race, () -> {
      Bukkit.getPluginManager().callEvent(new ChangeRaceNameEvent(race, oldName));
      MessageManager.setValue("old_name", oldName);
      MessageManager.setValue("new_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.CHANGE_RACE_NAME_SUCCESS);
    });
  }
}
