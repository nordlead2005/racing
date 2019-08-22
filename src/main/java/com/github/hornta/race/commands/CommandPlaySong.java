package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.SongManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPlaySong implements ICommandHandler {
  @Override
  public void handle(CommandSender sender, String[] args) {
    SongManager.playSong(args[0], (Player)sender);
  }
}
