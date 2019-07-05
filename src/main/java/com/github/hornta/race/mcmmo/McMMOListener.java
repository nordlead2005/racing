package com.github.hornta.race.mcmmo;

import com.github.hornta.race.RacingManager;
import com.github.hornta.race.enums.RaceState;
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
    if (raceSession.getState() == RaceState.COUNTDOWN || raceSession.getState() == RaceState.STARTED) {
      event.setCancelled(true);
    }
  }
}
