package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class CommandStartRace extends RacingCommand implements ICommandHandler {
  public CommandStartRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    if(race == null) {
      List<Race> allRaces = racingManager
        .getRaces()
        .stream()
        .filter((Race r) -> r.getState() == RaceState.ENABLED)
        .collect(Collectors.toList());

      if(allRaces.isEmpty()) {
        MessageManager.sendMessage(commandSender, MessageKey.START_RACE_NO_ENABLED);
        return;
      }
      race = allRaces.get(Util.randomRangeInt(0, allRaces.size() - 1));
    }

    int laps = args.length == 1 ? 1 : Integer.parseInt(args[1]);

    if(race.getState() != RaceState.ENABLED) {
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_NOT_ENABLED);
      return;
    }

    List<RaceSession> sessions = racingManager.getRaceSessions(race);

    if(!sessions.isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_ALREADY_STARTED);
      return;
    }

    if(race.getStartPoints().size() < 1) {
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_MISSING_STARTPOINT);
      return;
    }

    if(laps == 1 && race.getCheckpoints().size() < 1) {
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_MISSING_CHECKPOINT);
      return;
    } else if(race.getCheckpoints().size() < 2) {
      MessageManager.sendMessage(commandSender, MessageKey.START_RACE_MISSING_CHECKPOINTS);
      return;
    }

    racingManager.startNewSession(commandSender, race, laps);
  }
}
