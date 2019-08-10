package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceStatType;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RacePlayerStatistic;
import org.bukkit.command.CommandSender;

import java.util.Locale;
import java.util.Set;

public class CommandResetTop extends RacingCommand implements ICommandHandler {
  public CommandResetTop(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);
    race.resetResults();
    racingManager.updateRace(race, () -> {
      MessageManager.sendMessage(commandSender, MessageKey.RACE_RESET_TOP);
    });
  }
}

