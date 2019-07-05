package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.command.CommandSender;

public class StartPointExistValidator implements ValidationHandler {
  private RacingManager racingManager;
  private boolean shouldExist;

  public StartPointExistValidator(RacingManager racingManager, boolean shouldExist) {
    this.racingManager = racingManager;
    this.shouldExist = shouldExist;
  }

  private static boolean raceHasPoint(Race race, int position) {
    for(RaceStartPoint point : race.getStartPoints()) {
      if(point.getPosition() == position) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    Race race = racingManager.getRace(arguments[0]);
    int position = Integer.parseInt(arguments[1]);
    boolean hasPoint = raceHasPoint(race, position);
    return (shouldExist && hasPoint) || (!shouldExist && !hasPoint);
  }

  @Override
  public void whenInvalid(CommandSender sender, String[] arguments) {
    MessageKey key;
    if(this.shouldExist) {
      key = MessageKey.STARTPOINT_NOT_FOUND;
    } else {
      key = MessageKey.STARTPOINT_ALREADY_EXIST;
    }
    MessageManager.setValue("race_name", arguments[0]);
    MessageManager.setValue("position", arguments[1]);
    MessageManager.sendMessage(sender, key);
  }
}
