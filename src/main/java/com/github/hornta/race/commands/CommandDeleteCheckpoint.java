package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import org.bukkit.command.CommandSender;

public class CommandDeleteCheckpoint extends RacingCommand implements ICommandHandler {
  public CommandDeleteCheckpoint(RacingManager racingManager) {
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

    RaceCheckpoint checkpoint = race.getCheckpoint(Integer.parseInt(args[1]));
    racingManager.deleteCheckpoint(race, checkpoint, () -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.setValue("position", checkpoint.getPosition());

      MessageManager.sendMessage(commandSender, MessageKey.RACE_DELETE_CHECKPOINT_SUCCESS);
    });
  }
}
