package com.github.hornta.race.commands.completers;

import com.github.hornta.completers.ITabCompleter;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.objects.RacePotionEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RacePotionEffectCompleter implements ITabCompleter {
  private final RacingManager racingManager;

  public RacePotionEffectCompleter(RacingManager racingManager) {
    this.racingManager = racingManager;
  }

  @Override
  public List<String> getItems(CommandSender sender, String argument, String[] prevArgs) {
    return racingManager.getRace(prevArgs[0])
      .getPotionEffects()
      .stream()
      .map(RacePotionEffect::getType)
      .map(PotionEffectType::getName)
      .filter(type -> type.toLowerCase(Locale.ENGLISH).startsWith(argument.toLowerCase(Locale.ENGLISH)))
      .collect(Collectors.toList());
  }
}
