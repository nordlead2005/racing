package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCreateRace extends RacingCommand implements ICommandHandler {
  public CommandCreateRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Player player = (Player) commandSender;
    racingManager.createRace(Util.centerOnBlockHorizontally(player.getLocation()), args[0], (Race race) -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(player, MessageKey.CREATE_RACE_SUCCESS);
    });
  }
}
