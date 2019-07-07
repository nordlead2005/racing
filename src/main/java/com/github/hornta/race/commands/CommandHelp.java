package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandHelp implements ICommandHandler {
  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Player player = null;
    if(commandSender instanceof Player) {
      player = (Player)commandSender;
    }

    List<String> helpTexts = Racing.getInstance().getCarbon().getCommandManager().getHelpTexts(player);

    MessageManager.sendMessage(commandSender, MessageKey.RACE_HELP_TITLE);

    for(String helpText : helpTexts) {
      MessageManager.setValue("text", helpText);
      MessageManager.sendMessage(commandSender, MessageKey.RACE_HELP_ITEM);
    }
  }
}
