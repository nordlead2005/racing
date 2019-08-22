package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRaceSetSpawn extends RacingCommand implements ICommandHandler {
  public CommandRaceSetSpawn(RacingManager racingManager) {
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

    race.setSpawn(Util.centerOnBlockHorizontally(((Player) commandSender).getLocation()));

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.setValue("x", race.getSpawn().getX());
      MessageManager.setValue("y", race.getSpawn().getY());
      MessageManager.setValue("z", race.getSpawn().getZ());
      MessageManager.setValue("world", race.getSpawn().getWorld().getName());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_SPAWN_SUCCESS);
    });
  }
}
