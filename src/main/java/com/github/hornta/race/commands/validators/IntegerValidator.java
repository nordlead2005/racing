package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

public class IntegerValidator implements ValidationHandler {
  private int min = Integer.MIN_VALUE;
  private int max = Integer.MAX_VALUE;

  public IntegerValidator(int min) {
    this.min = min;
  }

  private enum Result {
    NON_INTEGER,
    MIN_EXCEED,
    MAX_EXCEED,
    OK
  };

  @Override
  public void whenInvalid(CommandSender commandSender, String[] strings) {
    Result result = getResult(strings[0]);

    switch (result) {
      case NON_INTEGER:
        MessageManager.setValue("received", strings[0]);
        MessageManager.sendMessage(commandSender, MessageKey.VALIDATE_INTEGER_NON_INTEGER);
        break;
      case MIN_EXCEED:
        MessageManager.setValue("expected", min);
        MessageManager.setValue("received", strings[0]);
        MessageManager.sendMessage(commandSender, MessageKey.VALIDATE_INTEGER_MIN_EXCEED);
        break;
      case MAX_EXCEED:
        MessageManager.setValue("expected", max);
        MessageManager.setValue("received", strings[0]);
        MessageManager.sendMessage(commandSender, MessageKey.VALIDATE_INTEGER_MAX_EXCEED);
        break;
      default:
    }
  }

  @Override
  public boolean test(CommandSender commandSender, String[] args) {
    return getResult(args[0]) == Result.OK;
  }

  private Result getResult(String arg) {
    int integer;
    try {
      integer = Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      return Result.NON_INTEGER;
    }

    if(integer < min) {
      return Result.MIN_EXCEED;
    }

    if(integer > max) {
      return Result.MAX_EXCEED;
    }

    return Result.OK;
  }
}
