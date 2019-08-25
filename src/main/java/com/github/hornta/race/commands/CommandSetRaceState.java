package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.events.RaceChangeStateEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandSetRaceState extends RacingCommand implements ICommandHandler {
  public CommandSetRaceState(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);
    RaceState newState = RaceState.fromString(args[1]);

    if(race.getState() == newState) {
      MessageManager.setValue("state", newState.name());
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_STATE_NOCHANGE);
      return;
    }

    if(race.getState() == RaceState.ENABLED && racingManager.hasOngoingSession(race)) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_STATE_ONGOING);
      return;
    }

    RaceState oldState = race.getState();
    race.setState(newState);

    racingManager.updateRace(race, () -> {
      Bukkit.getPluginManager().callEvent(new RaceChangeStateEvent(race));
      MessageManager.setValue("old_state", oldState);
      MessageManager.setValue("new_state", newState);
      MessageManager.sendMessage(commandSender, MessageKey.RACE_SET_STATE_SUCCESS);
    });
  }
}
