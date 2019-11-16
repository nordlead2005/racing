package com.github.hornta.race.objects;

import com.github.hornta.race.enums.RaceCommandType;
import com.github.hornta.race.events.ExecuteCommandEvent;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RaceCommandExecutor implements Listener {
  @EventHandler
  void onExecuteCommand(ExecuteCommandEvent event) {
    Race race = event.getRaceSession().getRace();
    List<RaceCommand> commands = race.getCommands().stream().filter((RaceCommand command) -> {
      return command.isEnabled() && command.getCommandType() == event.getCommandType();
    }).collect(Collectors.toList());

    for(RaceCommand command : commands) {
      if(
        command.getCommandType() == RaceCommandType.ON_RACE_FINISH ||
        command.getCommandType() == RaceCommandType.ON_PLAYER_FINISH
      ) {
        if(command.getRecipient() == Integer.MIN_VALUE) {
          MessageManager.setValue("race", race.getName());
          dispatchCommand(command);
        } else if(command.getRecipient() == 0) {
          for(PlayerSessionResult result : event.getRaceSession().getResult().getPlayerResults().values()) {
            MessageManager.setValue("player_name", result.getPlayerSession().getPlayerName());
            MessageManager.setValue("position", result.getPosition());
            MessageManager.setValue("time", result.getTime());
            MessageManager.setValue("race", race.getName());
            dispatchCommand(command);
          }
        } else {
          PlayerSessionResult result = event.getRaceSession().getResult().getResult(command.getRecipient());
          if(result != null) {
            MessageManager.setValue("player_name", result.getPlayerSession().getPlayerName());
            MessageManager.setValue("position", result.getPosition());
            MessageManager.setValue("time", result.getTime());
            MessageManager.setValue("race", race.getName());
            dispatchCommand(command);
          }
        }
      } else {
        MessageManager.setValue("race", race.getName());
        dispatchCommand(command);
      }
    }
  }

  private void dispatchCommand(RaceCommand command) {
    String formattedCommand = MessageManager.transformPlaceholders(command.getCommand());
    Bukkit.getLogger().log(Level.INFO, "Dispatching command: " + formattedCommand);
    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), formattedCommand);
  }
}
