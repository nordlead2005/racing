package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.MessageKey;
import com.github.hornta.carbon.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandStopRace extends RacingCommand implements ICommandHandler {
  public CommandStopRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args, int typedArgs) {
    Race race = racingManager.getRace(args[0]);
    List<RaceSession> sessions = racingManager.getRaceSessions(race);

    if(sessions.isEmpty()) {
      MessageManager.sendMessage(commandSender, MessageKey.STOP_RACE_NOT_STARTED);
      return;
    }

    for(RaceSession session : sessions) {
      session.stop();
    }

    MessageManager.broadcast(MessageKey.STOP_RACE_SUCCESS);
  }
}
