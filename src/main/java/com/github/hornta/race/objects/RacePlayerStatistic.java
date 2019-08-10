package com.github.hornta.race.objects;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class RacePlayerStatistic {
  private UUID playerId;
  private String playerName;
  private int wins;
  private int runs;
  private long time;

  public RacePlayerStatistic(UUID playerId, String playerName, int wins, int runs, long time) {
    this.playerId = playerId;
    this.playerName = playerName;
    this.wins = wins;
    this.runs = runs;
    this.time = time;
  }

  public RacePlayerStatistic clone() {
    return new RacePlayerStatistic(playerId, playerName, wins, runs, time);
  }

  public UUID getPlayerId() {
    return playerId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public long getTime() {
    return time;
  }

  public int getRuns() {
    return runs;
  }

  public int getWins() {
    return wins;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public void setRuns(int runs) {
    this.runs = runs;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public void setWins(int wins) {
    this.wins = wins;
  }
}
