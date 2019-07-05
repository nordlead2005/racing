package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import org.bukkit.command.CommandSender;

public class PointExistValidator implements ValidationHandler {
  private RacingManager racingManager;
  private boolean shouldExist;

  public PointExistValidator(RacingManager racingManager, boolean shouldExist) {
    this.racingManager = racingManager;
    this.shouldExist = shouldExist;
  }

  private static boolean raceHasPoint(Race race, int position) {
    for(RaceCheckpoint checkpoint : race.getCheckpoints()) {
      if(checkpoint.getPosition() == position) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    int position;
    try {
      position = Integer.parseInt(arguments[1]);
    } catch(NumberFormatException e) {
      return false;
    }
    Race race = racingManager.getRace(arguments[0]);
    boolean hasPoint = raceHasPoint(race, position);
    return (shouldExist && hasPoint) || (!shouldExist && !hasPoint);
  }

  @Override
  public void whenInvalid(CommandSender commandSender, String[] arguments) {
    MessageKey key;
    if(this.shouldExist) {
      key = MessageKey.CHECKPOINT_NOT_FOUND;
    } else {
      key = MessageKey.CHECKPOINT_ALREADY_EXIST;
    }

    MessageManager.setValue("race_name", arguments[0]);
    MessageManager.setValue("position", arguments[1]);

    MessageManager.sendMessage(commandSender, key);
  }
}
