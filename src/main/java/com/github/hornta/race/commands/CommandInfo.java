package com.github.hornta.race.commands;

import com.github.hornta.carbon.ICommandHandler;
import com.github.hornta.race.Racing;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RacePotionEffect;
import org.bukkit.command.CommandSender;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInfo extends RacingCommand implements ICommandHandler {
  public CommandInfo(RacingManager racingManager) {
    super(racingManager);
  }

  @Override
  public void handle(CommandSender commandSender, String[] args) {
    Race race = racingManager.getRace(args[0]);

    List<String> potionEffects = race.getPotionEffects().stream().map((RacePotionEffect effect) -> {
      MessageManager.setValue("potion_effect", effect.getType().getName());
      MessageManager.setValue("amplifier", effect.getAmplifier());
      return MessageManager.getMessage(MessageKey.RACE_INFO_POTION_EFFECT);
    }).collect(Collectors.toList());

    String entryFee = "";
    if(Racing.getInstance().getEconomy() != null) {
      MessageManager.setValue("entry_fee", Racing.getInstance().getEconomy().format(race.getEntryFee()));
      entryFee = MessageManager.getMessage(MessageKey.RACE_INFO_ENTRY_FEE_LINE);
    }

    String noPotionEffects = race.getPotionEffects().isEmpty() ?
      MessageManager.getMessage(MessageKey.RACE_INFO_NO_POTION_EFFECTS) : "";

    MessageManager.setValue("name", race.getName());
    MessageManager.setValue("type", race.getType().name());
    MessageManager.setValue("state", race.getState().name());
    DateTimeFormatter createdFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
      .withLocale(RaceConfiguration.getValue(ConfigKey.LOCALE))
      .withZone(ZoneId.systemDefault());
    MessageManager.setValue("created", createdFormatter.format(race.getCreatedAt()));
    MessageManager.setValue("num_startpoints", race.getStartPoints().size());
    MessageManager.setValue("num_checkpoints", race.getCheckpoints().size());
    MessageManager.setValue("entry_fee", entryFee);
    MessageManager.setValue("walk_speed", race.getWalkSpeed());
    MessageManager.setValue("none", noPotionEffects);
    MessageManager.setValue("potion_effects", potionEffects);

    MessageManager.sendMessage(commandSender, MessageKey.RACE_INFO_SUCCESS);
  }
}
