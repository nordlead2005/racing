package com.github.hornta.race.objects;

import java.time.Duration;

public class PlayerSessionResult {
  private final int position;
  private final Duration time;

  PlayerSessionResult(int position, Duration time) {
    this.position = position;
    this.time = time;
  }

  public int getPosition() {
    return position;
  }

  public Duration getTime() {
    return time;
  }
}
