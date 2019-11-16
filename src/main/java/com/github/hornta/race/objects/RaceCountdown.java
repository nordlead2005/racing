package com.github.hornta.race.objects;

import com.github.hornta.race.Racing;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

class RaceCountdown {
  private static final int HALF_SECOND = 10;
  private static final int ONE_SECOND = 20;
  private final Collection<RacePlayerSession> playerSessions;
  private int countdown;
  private BukkitRunnable task;

  RaceCountdown(Collection<RacePlayerSession> playerSessions) {
    this.playerSessions = playerSessions;
  }

  void start(Runnable callback) {
    this.countdown = RaceConfiguration.getValue(ConfigKey.COUNTDOWN);
    task = new BukkitRunnable() {
      @Override
      public void run() {
        if(countdown < 1) {
          cancel();
          callback.run();
          return;
        }

        for(RacePlayerSession session : playerSessions) {
          // show the title for a tick longer to prevent blinking between titles
          session.getPlayer().sendTitle(String.valueOf(countdown), MessageManager.getMessage(MessageKey.RACE_COUNTDOWN), 0, ONE_SECOND + 1, 0);
        }
        countdown -= 1;
      }
    };
    task.runTaskTimer(Racing.getInstance(), HALF_SECOND, ONE_SECOND);
  }

  void stop() {
    if(task != null) {
      task.cancel();
      task = null;
    }
  }
}
