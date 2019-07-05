package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandStartRace extends RacingCommand implements ICommandHandler {
  public CommandStartRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(!race.isEnabled()) {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_IS_DISABLED);
      return;
    }

    List<RaceSession> sessions = racingManager.getRaceSessions(race);

    if(!sessions.isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_ALREADY_STARTED);
      return;
    }

    int minStartPoints = RaceConfiguration.getValue(ConfigKey.MIN_REQUIRED_STARTPOINTS);
    if(race.getStartPoints().size() < minStartPoints) {
      MessageManager.setValue("num_start_points", minStartPoints);
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_MISSING_STARTPOINTS);
      return;
    }

    int minCheckpoints = RaceConfiguration.getValue(ConfigKey.MIN_REQUIRED_CHECKPOINTS);
    if(race.getCheckpoints().size() < minCheckpoints) {
      MessageManager.setValue("num_checkpoints", minCheckpoints);
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_MISSING_CHECKPOINT);
      return;
    }

    racingManager.startNewSession(commandSender, race);
  }
}
