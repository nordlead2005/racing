package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDeleteStartpoint extends RacingCommand implements ICommandHandler {
  public CommandDeleteStartpoint(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);

    if(race.getState() != RaceState.UNDER_CONSTRUCTION) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.EDIT_NO_EDIT_MODE);
      return;
    }

    Player player = (Player) commandSender;
    RaceStartPoint startPoint = race.getStartPoint(Integer.parseInt(args[1]));

    racingManager.deleteStartPoint(race, startPoint, () -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.setValue("position", String.valueOf(startPoint.getPosition()));
      MessageManager.sendMessage(player, MessageKey.RACE_DELETE_STARTPOINT_SUCCESS);
    });
  }
}
