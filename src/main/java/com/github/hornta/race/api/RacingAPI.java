package com.github.hornta.race.api;

import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RaceStartPoint;

import java.util.List;
import java.util.function.Consumer;

public interface RacingAPI {
  void fetchAllRaces(Consumer<List<Race>> callback);
  void createRace(Race race, Consumer<Boolean> callback);
  void deleteRace(Race race, Consumer<Boolean> callback);
  void updateRace(Race race, Consumer<Boolean> callback);
  void addRaceStart(Race race, RaceStartPoint startPoint, Consumer<Boolean> callback);
  void deleteStartPoint(Race race, RaceStartPoint startPoint, Consumer<Boolean> callback);
  void addStartPoint(Race race, RaceCheckpoint checkpoint, Consumer<Boolean> callback);
  void deletePoint(Race race, RaceCheckpoint checkpoint, Consumer<Boolean> callback);
}
