package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.SongManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStopSong implements ICommandHandler {
  @Override
  public void handle(CommandSender sender, String[] args, int typedArgs) {
    SongManager.stopSong((Player)sender);
  }
}
