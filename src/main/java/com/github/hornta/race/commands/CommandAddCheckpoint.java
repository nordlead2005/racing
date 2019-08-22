package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAddCheckpoint extends RacingCommand implements ICommandHandler {
  public CommandAddCheckpoint(RacingManager racingManager) {
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

    Player player = (Player)commandSender;

    if(race.getCheckpoint(player.getLocation()) != null) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_ADD_CHECKPOINT_IS_OCCUPIED);
      return;
    }

    racingManager.addCheckpoint(Util.centerOnBlock(player.getLocation()), race, (RaceCheckpoint checkPoint) -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.setValue("position", checkPoint.getPosition());
      MessageManager.sendMessage(player, MessageKey.RACE_ADD_CHECKPOINT_SUCCESS);
    });
  }
}
