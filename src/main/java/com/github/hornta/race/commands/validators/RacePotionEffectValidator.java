package com.github.hornta.race.commands.validators;

import com.github.hornta.ValidationHandler;
import com.github.hornta.ValidationResult;
import com.github.hornta.race.RacingManager;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RacePotionEffect;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

public class RacePotionEffectValidator implements ValidationHandler {
  private RacingManager racingManager;

  public RacePotionEffectValidator(RacingManager racingManager) {
    this.racingManager = racingManager;
  }
  @Override
  public boolean test(CommandSender commandSender, String argument, String[] prevArgs) {
    Race race = racingManager.getRace(prevArgs[0]);
    return race.getPotionEffects()
      .stream()
      .map(RacePotionEffect::getType)
      .map(PotionEffectType::getName)
      .anyMatch((String name) -> name.equalsIgnoreCase(argument));
  }

  @Override
  public void whenInvalid(ValidationResult result) {
    MessageManager.setValue("race_name", result.getPrevArgs()[0]);
    MessageManager.setValue("potion_effect", result.getValue());
    MessageManager.sendMessage(result.getCommandSender(), MessageKey.RACE_POTION_EFFECT_NOT_FOUND);
  }
}
