package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

public class CommandSetSong extends RacingCommand implements ICommandHandler {
  public CommandSetSong(RacingManager racingManager) {
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

    if(args[1].equals(race.getSong())) {
      MessageManager.setValue("song", race.getSong());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_SONG_NOCHANGE);
      return;
    }

    String oldSong = race.getSong();
    race.setSong(args[1]);

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("old_song", oldSong);
      MessageManager.setValue("new_song", args[0]);
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_SONG_SUCCESS);
    });
  }
}

