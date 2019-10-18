package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.JoinType;
import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandJoinRace extends RacingCommand implements ICommandHandler {
  public CommandJoinRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);
    Player player = (Player)commandSender;
    racingManager.joinRace(race, player, JoinType.COMMAND);
  }
}
