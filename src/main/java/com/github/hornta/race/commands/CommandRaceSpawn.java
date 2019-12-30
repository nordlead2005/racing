package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.enums.Permission;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import com.github.hornta.race.objects.Race;
import io.papermc.lib.PaperLib;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CommandRaceSpawn extends RacingCommand implements ICommandHandler {
  public CommandRaceSpawn(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);

    if(race.getState() != RaceState.ENABLED && !commandSender.hasPermission(Permission.RACING_MODERATOR.toString())) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SPAWN_NOT_ENABLED);
      return;
    }

    PaperLib.teleportAsync(((Player)commandSender), Util.snapAngles(race.getSpawn()), PlayerTeleportEvent.TeleportCause.COMMAND);
  }
}
