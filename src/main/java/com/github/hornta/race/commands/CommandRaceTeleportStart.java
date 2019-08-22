package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CommandRaceTeleportStart extends RacingCommand implements ICommandHandler {
  public CommandRaceTeleportStart(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    RaceStartPoint checkpoint = racingManager.getRace(args[0]).getStartPoint(Integer.parseInt(args[1]));
    Player player = (Player)commandSender;

    Location loc = Util.snapAngles(checkpoint.getLocation());
    player.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
  }
}
