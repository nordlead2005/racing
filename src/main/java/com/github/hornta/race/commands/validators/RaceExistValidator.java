package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

public class RaceExistValidator implements ValidationHandler {
  private RacingManager racingManager;
  private boolean shouldExist;

  public RaceExistValidator(RacingManager racingManager, boolean shouldExist) {
    this.racingManager = racingManager;
    this.shouldExist = shouldExist;
  }

  @Override
  public boolean test(CommandSender commandSender, String[] arguments) {
    if(shouldExist) {
      return racingManager.hasRace(arguments[0]);
    } else {
      return !racingManager.hasRace(arguments[0]);
    }
  }

  @Override
  public void whenInvalid(CommandSender sender, String[] args) {
    MessageKey message;
    if(this.shouldExist) {
      message = MessageKey.RACE_NOT_FOUND;
    } else {
      message = MessageKey.RACE_ALREADY_EXIST;
    }
    MessageManager.setValue("race_name", args[0]);
    MessageManager.sendMessage(sender, message);
  }
}
