package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.api.ParseRaceException;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.events.ConfigReloadedEvent;
import com.github.hornta.race.message.Translation;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandReload implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    try {
      RaceConfiguration.reload(Racing.getInstance());
    } catch (ParseRaceException e) {
      MessageManager.setValue("error", e.getMessage());
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_FAILED);
      return;
    }

    Bukkit.getPluginManager().callEvent(new ConfigReloadedEvent());

    String language = RaceConfiguration.getValue(ConfigKey.LANGUAGE);
    Translation translation = Racing.getInstance().getTranslations().createTranslation(language);
    if(translation == null || !translation.load()) {
      MessageManager.setValue("language", language);
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_NOT_LANGUAGE);
    }
    MessageManager.setTranslation(translation);

    if(!Racing.getInstance().getRacingManager().getRaceSessions().isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_NOT_RACES);
    } else {
      Racing.getInstance().getRacingManager().load();
    }

    MessageManager.sendMessage(commandSender, MessageKey.RELOAD_SUCCESS);
  }
}
