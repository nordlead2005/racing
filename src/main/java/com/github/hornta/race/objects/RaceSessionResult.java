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

  public void addPlayerSessionResult(RacePlayerSession playerSession, int position, long time) {
    playerResults.put(playerSession, new PlayerSessionResult(playerSession, position, time));
  }

  public Map<RacePlayerSession, PlayerSessionResult> getPlayerResults() {
    return playerResults;
  }
}
