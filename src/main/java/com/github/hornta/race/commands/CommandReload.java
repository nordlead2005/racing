package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.command.CommandSender;

public class CommandReload implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args) {
    RaceConfiguration.reload(Racing.getInstance());
    SongManager.getInstance().loadSongs((String) RaceConfiguration.getValue(ConfigKey.SONGS_DIRECTORY));

    String language = RaceConfiguration.getValue(ConfigKey.LANGUAGE);
    if(!Racing.getInstance().getTranslations().selectLanguage(language)) {
      MessageManager.setValue("language", language);
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_NOT_LANGUAGE);
    }
    MessageManager.setLanguageTranslation(Racing.getInstance().getTranslations().getSelectedLanguage());

    if(!Racing.getInstance().getRacingManager().getRaceSessions().isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_NOT_RACES);
    } else {
      Racing.getInstance().getRacingManager().load();
    }

    MessageManager.sendMessage(commandSender, MessageKey.RELOAD_SUCCESS);
  }
}
