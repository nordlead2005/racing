package com.github.hornta.race.objects;

import com.github.hornta.race.Racing;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

class RaceCountdown {
  public static final int COUNTDOWN_IN_SECONDS = 10;
  private static final int HALF_SECOND = 10;
  private static final int ONE_SECOND = 20;
  private Map<Player, RacePlayerSession> playerSessions;
  private int countdown = COUNTDOWN_IN_SECONDS;
  private BukkitRunnable task;

  RaceCountdown(Map<Player, RacePlayerSession> playerSessions) {
    this.playerSessions = playerSessions;
  }

  void start(Runnable callback) {
    this.countdown = COUNTDOWN_IN_SECONDS;
    task = new BukkitRunnable() {
      @Override
      public void run() {
        if(countdown == 0) {
          cancel();
          callback.run();
          return;
        }

        for(RacePlayerSession session : playerSessions.values()) {
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
