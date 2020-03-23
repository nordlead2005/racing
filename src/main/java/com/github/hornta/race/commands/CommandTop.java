package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.carbon.message.MessageManager;
import com.github.hornta.race.MessageKey;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.enums.RaceStatType;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RacePlayerStatistic;

import org.bukkit.command.CommandSender;

import java.util.Locale;
import java.util.Set;

public class CommandTop extends RacingCommand implements ICommandHandler {
  public CommandTop(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);
    RaceStatType statType = RaceStatType.valueOf(args[1].toUpperCase(Locale.ENGLISH));
    int laps = Integer.parseInt(args[2]);
    
    sendTopMessage(commandSender, race, laps, statType);
  }

  static public void sendTopMessage(CommandSender target, Race race, int laps, RaceStatType statType) {
    MessageManager.setValue("type", MessageManager.getMessage(statType.getKey()));
    MessageManager.setValue("laps", laps);
    MessageManager.setValue("race_name", race.getName());
    MessageManager.sendMessage(target, MessageKey.RACE_TOP_HEADER);
  
    Set<RacePlayerStatistic> results = (statType == RaceStatType.FASTEST) ? race.getResultsForLapCount(laps) : race.getResults(statType);
    int i = 1;
    for(RacePlayerStatistic result : results) {

      String value = "";
      switch (statType) {
        case WIN_RATIO:
          value = (int)((float)result.getWins() / result.getRuns() * 100) + "%";
          break;
        case FASTEST:
          value = Util.getTimeLeft(result.getRecord(laps));
          break;
        case FASTEST_LAP:
          value = Util.getTimeLeft(result.getFastestLap());
          break;
        case WINS:
          value = result.getWins() + "";
          break;
        case RUNS:
          value = result.getRuns() + "";
          break;
        default:
      }

      Util.setTimeUnitValues();
      MessageManager.setValue("position", i++);
      MessageManager.setValue("value", value);
      MessageManager.setValue("player_name", result.getPlayerName());
      MessageManager.sendMessage(target, MessageKey.RACE_TOP_ITEM);
  
      if (i == 10) {
        break;
      }
    }
  
    for(int k = i; k < 10; k++) {
      MessageManager.setValue("position", k);
      MessageManager.sendMessage(target, MessageKey.RACE_TOP_ITEM_NONE);
    }
  }
}

