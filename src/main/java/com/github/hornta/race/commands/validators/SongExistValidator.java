package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

public class SongExistValidator implements ValidationHandler {
  @Override
  public boolean test(CommandSender sender, String[] args) {
    return SongManager.getSongByName(args[0]) != null;
  }

  @Override
  public void whenInvalid(CommandSender commandSender, String[] args) {
    MessageManager.setValue("song_name", args[0]);
    MessageManager.sendMessage(commandSender, MessageKey.SONG_NOT_FOUND);
  }
}
