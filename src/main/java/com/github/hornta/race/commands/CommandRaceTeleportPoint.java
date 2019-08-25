package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.objects.RaceCheckpoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CommandRaceTeleportPoint extends RacingCommand implements ICommandHandler {
  public CommandRaceTeleportPoint(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    RaceCheckpoint checkpoint = racingManager.getRace(args[0]).getCheckpoint(Integer.parseInt(args[1]));
    Player player = (Player)commandSender;
    player.teleport(checkpoint.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
  }
}
