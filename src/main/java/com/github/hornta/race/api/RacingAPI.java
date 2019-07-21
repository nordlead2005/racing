package com.github.hornta.race.api;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RaceStartPoint;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface RacingAPI {
  void fetchAllRaces(Consumer<List<Race>> callback);
  void deleteRace(Race race, Consumer<Boolean> callback);
  void updateRace(Race race, Consumer<Boolean> callback);
  void addStartPoint(UUID raceId, RaceStartPoint startPoint, Consumer<Boolean> callback);
  void deleteStartPoint(UUID raceId, RaceStartPoint startPoint, Consumer<Boolean> callback);
  void addCheckpoint(UUID raceId, RaceCheckpoint checkpoint, Consumer<Boolean> callback);
  void deleteCheckpoint(UUID raceId, RaceCheckpoint checkpoint, Consumer<Boolean> callback);
}
