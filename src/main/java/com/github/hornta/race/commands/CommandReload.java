package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.carbon.message.Translation;
import com.github.hornta.race.ConfigKey;
import com.github.hornta.race.Racing;
import com.github.hornta.race.api.ParseRaceException;
import com.github.hornta.race.events.ConfigReloadedEvent;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandReload implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    try {
      Racing.getInstance().getConfiguration().reload();
    } catch (Exception e) {
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_FAILED);
      return;
    }

    Bukkit.getPluginManager().callEvent(new ConfigReloadedEvent());

    Translation translation = Racing.getInstance().getTranslations().createTranslation(Racing.getInstance().getConfiguration().get(ConfigKey.LANGUAGE));
    MessageManager.getInstance().setPrimaryTranslation(translation);

    if(!Racing.getInstance().getRacingManager().getRaceSessions().isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.RELOAD_NOT_RACES);
    } else {
      try {
        Racing.getInstance().getRacingManager().load();
      } catch (ParseRaceException e) {
        MessageManager.setValue("error", e.getMessage());
        MessageManager.sendMessage(commandSender, MessageKey.RELOAD_RACES_FAILED);
        return;
      }
    }

    MessageManager.sendMessage(commandSender, MessageKey.RELOAD_SUCCESS);
  }
}
