package com.github.hornta.race.commands;

import com.github.hornta.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandJoinRace extends RacingCommand implements ICommandHandler {
  public CommandJoinRace(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);
    List<RaceSession> sessions = racingManager.getRaceSessions(race);
    RaceSession session = null;
    if(!sessions.isEmpty()) {
      session = sessions.get(0);
    }

    if(session == null || session.getState() != RaceSessionState.PREPARING) {
      MessageManager.sendMessage(commandSender, MessageKey.JOIN_RACE_NOT_OPEN);
      return;
    }

    Player player = (Player)commandSender;

    if(session.isParticipating(player)) {
      MessageManager.sendMessage(commandSender, MessageKey.JOIN_RACE_IS_PARTICIPATING);
      return;
    }

    if(racingManager.getParticipatingRace(player) != null) {
      MessageManager.sendMessage(commandSender, MessageKey.JOIN_RACE_IS_PARTICIPATING_OTHER);
      return;
    }

    if(session.isFull()) {
      MessageManager.sendMessage(commandSender, MessageKey.JOIN_RACE_IS_FULL);
      return;
    }

    Economy economy = Racing.getInstance().getEconomy();
    if (economy != null) {
      if(economy.getBalance(player) < race.getEntryFee()) {
        MessageManager.setValue("entry_fee", economy.format(race.getEntryFee()));
        MessageManager.setValue("balance", economy.format(economy.getBalance(player)));
        MessageManager.sendMessage(commandSender, MessageKey.JOIN_RACE_NOT_AFFORD);
        return;
      }

      economy.withdrawPlayer(player, race.getEntryFee());
      MessageManager.setValue("fee", economy.format(race.getEntryFee()));
      MessageManager.sendMessage(player, MessageKey.JOIN_RACE_CHARGED);
    }

    session.participate(player, race.getEntryFee());

    MessageManager.setValue("player_name", player.getName());
    MessageManager.setValue("race_name", race.getName());
    MessageManager.setValue("current_participants", session.getAmountOfParticipants());
    MessageManager.setValue("max_participants", race.getStartPoints().size());
    MessageManager.broadcast(MessageKey.JOIN_RACE_SUCCESS);
  }
}
