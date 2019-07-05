package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandSetSong extends RacingCommand implements ICommandHandler {
  public CommandSetSong(RacingManager racingManager) {
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

    if(race.getSong().equals(args[1])) {
      MessageManager.setValue("song", race.getSong());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_SONG_NOCHANGE);
      return;
    }

    String oldSong = race.getSong();
    race.setSong(args[1]);

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("old_type", oldSong);
      MessageManager.setValue("new_type", args[0]);
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_SONG_SUCCESS);
    });
  }
}

