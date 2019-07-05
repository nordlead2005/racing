package com.github.hornta.race.objects;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceSessionResult {
  private final RaceSession raceSession;
  private Map<RacePlayerSession, PlayerSessionResult> playerResults = new HashMap<>();

  public RaceSessionResult(RaceSession raceSession) {
    this.raceSession = raceSession;
  }

  public RaceSession getRaceSession() {
    return raceSession;
  }

  public void addPlayerRessionResult(RacePlayerSession playerSession, int position, Duration time) {
    playerResults.put(playerSession, new PlayerSessionResult(position, time));
  }

  public Map<RacePlayerSession, PlayerSessionResult> getPlayerResults() {
    return playerResults;
  }
}
