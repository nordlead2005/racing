package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;

public class CommandRaces extends RacingCommand implements ICommandHandler {
  public CommandRaces(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    List<Race> races = racingManager.getRaces();

    MessageManager.setValue("races", races
      .stream()
      .map((Race race) -> {
        MessageManager.setValue("race", race.getName());
        return MessageManager.getMessage(MessageKey.LIST_RACES_ITEM);
      })
      .collect(Collectors.joining("§f, ")));
    MessageManager.setValue("num_race", races.size());
    MessageManager.sendMessage(commandSender, MessageKey.LIST_RACES_LIST);
  }
}
