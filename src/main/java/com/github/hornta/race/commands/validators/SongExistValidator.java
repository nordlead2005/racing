package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.ValidationResult;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

public class SongExistValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender sender, String argument, String[] prevArgs) {
    return SongManager.getSongByName(argument) != null;
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("song_name", result.getValue());
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.SONG_NOT_FOUND);
  }
}
