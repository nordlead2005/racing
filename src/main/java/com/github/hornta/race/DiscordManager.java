package com.github.hornta.race;

import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.events.ConfigReloadedEvent;
import com.github.hornta.race.events.RaceSessionStartEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.security.auth.login.LoginException;
import java.util.Comparator;
import java.util.logging.Level;

public class DiscordManager implements Listener, EventListener {
  private JDA api;
  private TextChannel announceChannel;
  private boolean startAfterShutdown = false;

  DiscordManager() {
    if(RaceConfiguration.getValue(ConfigKey.DISCORD_ENABLED)) {
      startup();
    }
  }

  private void startup() {
    startAfterShutdown = false;
    try {
      api = new JDABuilder(AccountType.BOT)
        .setToken(RaceConfiguration.getValue(ConfigKey.DISCORD_TOKEN))
        .addEventListener(this)
        .build();
    } catch (LoginException e) {
      Racing.logger().log(Level.SEVERE, "Failed to integrate with Discord, the bot token was incorrect.");
    }
  }

  @EventHandler
  void onConfigReloaded(ConfigReloadedEvent event) {
    if(api != null) {
      api.shutdown();
    }

    if(RaceConfiguration.getValue(ConfigKey.DISCORD_ENABLED)) {
      startAfterShutdown = true;
    }
  }

  @EventHandler
  void onRaceSessionStart(RaceSessionStartEvent event) {
    if(!(boolean)RaceConfiguration.getValue(ConfigKey.DISCORD_ENABLED)) {
      return;
    }

    MessageKey key;
    if (event.getRaceSession().getRace().getEntryFee() > 0) {
      key = MessageKey.PARTICIPATE_DISCORD_FEE;
    } else {
      key = MessageKey.PARTICIPATE_DISCORD;
    }

    int prepareTime = RaceConfiguration.getValue(ConfigKey.RACE_PREPARE_TIME);

    Util.setTimeUnitValues();

    MessageManager.setValue("race_name", event.getRaceSession().getRace().getName());
    MessageManager.setValue("command_sender", event.getRaceSession().getInitiator().getName());
    MessageManager.setValue("time_left", Util.getTimeLeft(prepareTime));
    MessageManager.setValue("laps", event.getRaceSession().getLaps());

    Economy economy = Racing.getInstance().getEconomy();
    if(economy != null) {
      MessageManager.setValue("entry_fee", economy.format(event.getRaceSession().getRace().getEntryFee()));
    }

    announceChannel.sendMessage(MessageManager.getMessage(key)).queue();
  }

  @Override
  public void onEvent(Event event) {
    if (event instanceof ShutdownEvent) {
      if(startAfterShutdown) {
        startup();
      }
    } else if (event instanceof ReadyEvent) {
      Racing.logger().log(Level.INFO, "Successful integration with Discord.");
      String channelId = RaceConfiguration.getValue(ConfigKey.DISCORD_ANNOUNCE_CHANNEL);
      if(channelId.isEmpty()) {
        announceChannel = api
          .getTextChannels()
          .stream()
          .min(Comparator.comparingInt(TextChannel::getPosition))
          .orElse(null);
      } else {
        announceChannel = api.getTextChannelById(channelId);
      }

      if (announceChannel == null) {
        Racing.logger().log(Level.SEVERE, "Couldn't find Discord channel with id: " + channelId);
      } else {
        Racing.logger().log(Level.INFO, "Found Discord channel " + announceChannel.getName() + " with id: " + announceChannel.getId());
      }
    }
  }
}
