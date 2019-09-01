package com.github.hornta.race.mcmmo;

import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.objects.RaceSession;
import com.gmail.nossr50.events.hardcore.McMMOPlayerDeathPenaltyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McMMOListener implements Listener {
  private RacingManager racingManager;

  public McMMOListener(RacingManager racingManager) {
    this.racingManager = racingManager;
  }

  @EventHandler
  void onMcMMOPlayerDeathPenalty(McMMOPlayerDeathPenaltyEvent event) {
    RaceSession raceSession = racingManager.getParticipatingRace(event.getPlayer());
    if (raceSession != null && (raceSession.getState() == RaceSessionState.COUNTDOWN || raceSession.getState() == RaceSessionState.STARTED)) {
      event.setCancelled(true);
    }
  }
}
