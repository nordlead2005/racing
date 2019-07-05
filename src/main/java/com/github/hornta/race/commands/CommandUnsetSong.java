package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandUnsetSong extends RacingCommand implements ICommandHandler {
  public CommandUnsetSong(RacingManager racingManager) {
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

    if(race.getSong() == null) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_UNSET_SONG_ALREADY_UNSET);
      return;
    }

    race.setSong(null);

    racingManager.updateRace(race, () -> {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_UNSET_SONG_SUCCESS);
    });
  }
}

