package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRaceSpawn extends RacingCommand implements ICommandHandler {
  public CommandRaceSpawn(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);
    if(race.isEditing() && !commandSender.hasPermission("ts.race.spawn.editing")) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SPAWN_IS_EDITING);
      return;
    }

    if(!race.isEnabled() && !commandSender.hasPermission("ts.race.spawn.disabled")) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_IS_DISABLED);
      return;
    }

    ((Player)commandSender).teleport(Util.snapAngles(race.getSpawn()));
  }
}
