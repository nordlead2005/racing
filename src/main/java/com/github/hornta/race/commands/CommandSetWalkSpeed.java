package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.events.RaceChangeStateEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandSetWalkSpeed extends RacingCommand implements ICommandHandler {
  public CommandSetWalkSpeed(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);
    float speed = Float.parseFloat(args[1]);

    if(race.getState() != RaceState.UNDER_CONSTRUCTION) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.EDIT_NO_EDIT_MODE);
      return;
    }

    race.setWalkSpeed(speed);

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("walk_speed", speed);
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_WALKSPEED);
    });
  }
}
