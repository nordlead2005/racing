package com.github.hornta.race.objects;

import java.util.HashMap;
import java.util.Map;

public class RaceSessionResult {
  private final RaceSession raceSession;
  private Map<RacePlayerSession, PlayerSessionResult> playerResults = new HashMap<>();
  private Map<Integer, PlayerSessionResult> resultsByPosition = new HashMap<>();

  public RaceSessionResult(RaceSession raceSession) {
    this.raceSession = raceSession;
  }

  public RaceSession getRaceSession() {
    return raceSession;
  }

  public void addPlayerSessionResult(RacePlayerSession playerSession, int position, long time) {
    PlayerSessionResult result = new PlayerSessionResult(playerSession, position, time);
    playerResults.put(playerSession, result);
    resultsByPosition.put(result.getPosition(), result);
  }

  public PlayerSessionResult getResult(int position) {
    return resultsByPosition.get(position);
  }

  public Map<RacePlayerSession, PlayerSessionResult> getPlayerResults() {
    return playerResults;
  }
}
