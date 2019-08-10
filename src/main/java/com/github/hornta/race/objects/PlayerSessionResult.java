package com.github.hornta.race.objects;

public class PlayerSessionResult {
  private final RacePlayerSession playerSession;
  private final int position;
  private final long time;

  PlayerSessionResult(RacePlayerSession playerSession, int position, long time) {
    this.playerSession = playerSession;
    this.position = position;
    this.time = time;
  }

  public RacePlayerSession getPlayerSession() {
    return playerSession;
  }

  public int getPosition() {
    return position;
  }

  public long getTime() {
    return time;
  }
}
