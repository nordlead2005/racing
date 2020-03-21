package com.github.hornta.race.objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;

import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.enums.RaceVersion;
// import com.github.hornta.race.enums.StartOrder;
import com.github.hornta.race.objects.RaceSession;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RaceSessionTests {
  @Test
  @DisplayName("Sort Test")
  void SortFastest() {
    
    final UUID uuid = new UUID(123456789, 987654321);
    Location spawn = new Location(Bukkit.getWorld("world"), 0, 62, 0);
    List<RaceCheckpoint> checkpoints = new ArrayList<RaceCheckpoint>();
    List<RaceStartPoint> startPoints = new ArrayList<RaceStartPoint>();
    Set<RacePotionEffect> potionEffects = new HashSet<RacePotionEffect>();
    Set<RaceSign> raceSigns = new HashSet<RaceSign>();
    Set<RacePlayerStatistic> results = new HashSet<RacePlayerStatistic>();
    List<RaceCommand> commands = new ArrayList<RaceCommand>();
    Race race = new Race(
      uuid,
      RaceVersion.V12,
      "TestRace",
      spawn,
      RaceState.ENABLED,
      Instant.ofEpochSecond(123456789),
      checkpoints,
      startPoints,
      RaceType.PLAYER,
      // StartOrder.FASTEST_LAP,
      "",
      (double)0.0,
      (float)1.0,
      potionEffects,
      raceSigns,
      results,
      1,
      1.0,
      1.0,
      1.0,
      commands);

    RaceSession session = new RaceSession(null, race, 1);
    assertTrue(true);
  }
}