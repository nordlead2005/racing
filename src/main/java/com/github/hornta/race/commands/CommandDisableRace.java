package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandDisableRace extends RacingCommand implements ICommandHandler {
  public CommandDisableRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    List<RaceSession> sessions = racingManager.getRaceSessions(race);

    if(!sessions.isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.DISABLE_RACE_IS_STARTED);
      return;
    }

    if(!race.isEnabled()) {
      MessageManager.sendMessage(commandSender, MessageKey.DISABLE_RACE_IS_DISABLED);
      return;
    }

    race.setEnabled(false);

    racingManager.updateRace(race, () -> {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.DISABLE_RACE_SUCCESS);
    });
  }
}
