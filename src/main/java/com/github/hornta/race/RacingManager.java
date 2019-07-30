package com.github.hornta.race;

import com.github.hornta.race.api.RacingAPI;
import com.github.hornta.race.enums.*;
import com.github.hornta.race.events.*;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.PlayerSessionResult;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RacePlayerSession;
import com.github.hornta.race.objects.RaceSession;
import com.github.hornta.race.objects.RaceStartPoint;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RacingManager implements Listener {
  private Map<String, Race> racesByName = new HashMap<>();
  private List<Race> races = new ArrayList<>();
  private RacingAPI api;
  private List<RaceSession> raceSessions = new ArrayList<>();

  public void shutdown() {
    for(RaceSession raceSession : raceSessions) {
      raceSession.stop();
    }
    raceSessions.clear();
  }

  public void startNewSession(CommandSender initiator, Race race, int laps) {
    RaceSession raceSession = new RaceSession(initiator, race, laps);
    raceSession.start();
    raceSessions.add(raceSession);
  }

  public List<RaceSession> getRaceSessions() {
    return new ArrayList<>(raceSessions);
  }

  public List<RaceSession> getRaceSessions(Race race) {
    return getRaceSessions(race, null);
  }

  public List<RaceSession> getRaceSessions(Race race, RaceSessionState state) {
    List<RaceSession> sessions = new ArrayList<>();

    for(RaceSession session : raceSessions) {
      boolean stateOk = state == null || session.getState() == state;
      if(session.getRace() == race && stateOk) {
        sessions.add(session);
      }
    }

    return sessions;
  }

  public boolean hasOngoingSession(Race race) {
    for(RaceSession session : raceSessions) {
      if(session.getRace() == race) {
        return true;
      }
    }
    return false;
  }

  @EventHandler
  void onCreateRace(CreateRaceEvent event) {
    if(event.getRace().getState() != RaceState.UNDER_CONSTRUCTION) {
      return;
    }

    for(RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      checkpoint.startTask(true);
      checkpoint.setupHologram();
    }

    for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
      startPoint.setupHologram();
    }
  }

  @EventHandler
  void onDeleteRace(DeleteRaceEvent event) {
    for(RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      checkpoint.stopTask();
      checkpoint.removeHologram();
    }

    for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
      startPoint.removeHologram();
    }
  }

  @EventHandler
  void onAddRaceCheckpoint(AddRaceCheckpointEvent event) {
    event.getCheckpoint().startTask(true);
    event.getCheckpoint().setupHologram();
  }

  @EventHandler
  void onDeleteRaceCheckpoint(DeleteRaceCheckpointEvent event) {
    event.getCheckpoint().stopTask();
    event.getCheckpoint().removeHologram();

    for (RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      if (checkpoint.getPosition() >= event.getCheckpoint().getPosition() && checkpoint.getHologram() != null) {
        checkpoint.removeHologram();
        checkpoint.setupHologram();
      }
    }
  }

  @EventHandler
  void onAddRaceStartPoint(AddRaceStartPointEvent event) {
    event.getStartPoint().setupHologram();
  }

  @EventHandler
  void onDeleteRaceStartPoint(DeleteRaceStartPointEvent event) {
    event.getStartPoint().removeHologram();

    for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
      if (startPoint.getPosition() >= event.getStartPoint().getPosition() && startPoint.getHologram() != null) {
        startPoint.removeHologram();
        startPoint.setupHologram();
      }
    }
  }

  @EventHandler
  void onRaceChangeState(RaceChangeStateEvent event) {
    for (RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      if (event.getRace().getState() == RaceState.UNDER_CONSTRUCTION) {
        checkpoint.startTask(true);
        checkpoint.setupHologram();
      } else {
        checkpoint.stopTask();
        checkpoint.removeHologram();
      }
    }

    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
        if (event.getRace().getState() == RaceState.UNDER_CONSTRUCTION) {
          startPoint.setupHologram();
        } else {
          startPoint.removeHologram();
        }
      }
    }
  }

  @EventHandler
  void onChangeRaceName(RaceChangeNameEvent event) {
    racesByName.remove(event.getOldName());
    racesByName.put(event.getRace().getName(), event.getRace());
  }

  @EventHandler
  void onRaceSessionResult(RaceSessionResultEvent event) {
    for(Map.Entry<RacePlayerSession, PlayerSessionResult> entry : event.getResult().getPlayerResults().entrySet()) {
      if(entry.getValue().getPosition() == 1) {
        MessageManager.setValue("player_name", entry.getKey().getPlayer().getName());
        MessageManager.setValue("race_name", event.getResult().getRaceSession().getRace().getName());
        MessageManager.setValue("time", Util.getTimeLeft(entry.getValue().getTime()));
        Util.setTimeUnitValues();
        MessageManager.broadcast(MessageKey.RACE_WIN);
        break;
      }
    }
  }

  @EventHandler
  void onRaceSessionStop(RaceSessionStopEvent event) {
    raceSessions.remove(event.getRaceSession());
  }

  @EventHandler
  void onPlayerJoin(PlayerJoinEvent event) {
    boolean hasPermission = event.getPlayer().hasPermission(Permission.RACING_MODIFY.toString());
    for(Race race : races) {
      for(RaceCheckpoint checkpoint : race.getCheckpoints()) {
        if(checkpoint.getHologram() != null) {
          if(hasPermission) {
            checkpoint.getHologram().getVisibilityManager().showTo(event.getPlayer());
          } else {
            checkpoint.getHologram().getVisibilityManager().hideTo(event.getPlayer());
          }
        }
      }

      for(RaceStartPoint startPoint : race.getStartPoints()) {
        if(startPoint.getHologram() != null) {
          if(hasPermission) {
            startPoint.getHologram().getVisibilityManager().showTo(event.getPlayer());
          } else {
            startPoint.getHologram().getVisibilityManager().hideTo(event.getPlayer());
          }
        }
      }
    }
  }

  @EventHandler
  void onPlayerQuit(PlayerQuitEvent event) {
    for(Race race : races) {
      for(RaceCheckpoint checkpoint : race.getCheckpoints()) {
        if(checkpoint.getHologram() != null) {
          checkpoint.getHologram().getVisibilityManager().resetVisibility(event.getPlayer());
        }
      }

      for(RaceStartPoint startPoint : race.getStartPoints()) {
        if(startPoint.getHologram() != null) {
          startPoint.getHologram().getVisibilityManager().resetVisibility(event.getPlayer());
        }
      }
    }
  }

  public void setAPI(RacingAPI api) {
    this.api = api;
  }

  public void load() {
    if(!raceSessions.isEmpty()) {
      throw new RuntimeException("Can't load races because there are ongoing race sessions.");
    }

    for(Race race : races) {
      Bukkit.getPluginManager().callEvent(new DeleteRaceEvent(race));
    }
    racesByName.clear();
    races.clear();

    api.fetchAllRaces((List<Race> fetchedRaces) -> Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      for(Race race : fetchedRaces) {
        racesByName.put(race.getName(), race);
        races.add(race);
        Bukkit.getPluginManager().callEvent(new CreateRaceEvent(race));
      }
    }));
  }

  public void updateRace(Race race, Runnable runnable) {
    api.updateRace(race, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        runnable.run();
      }
    }));
  }

  public void addCheckpoint(Location location, Race race, Consumer<RaceCheckpoint> consumer) {
    RaceCheckpoint checkpoint = new RaceCheckpoint(UUID.randomUUID(), race.getCheckpoints().size() + 1, location, 3);

    api.addCheckpoint(race.getId(), checkpoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.addStartPoint(checkpoint);
        Bukkit.getPluginManager().callEvent(new AddRaceCheckpointEvent(race, checkpoint));
        consumer.accept(checkpoint);
      }
    }));
  }

  public void deleteCheckpoint(Race race, RaceCheckpoint checkpoint, Runnable runnable) {
    api.deleteCheckpoint(race.getId(), checkpoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.setCheckpoints(
          race.
            getCheckpoints()
            .stream()
            .filter((RaceCheckpoint checkpoint1) -> checkpoint1 != checkpoint)
            .peek((RaceCheckpoint checkpoint1) -> {
              if(checkpoint1.getPosition() > checkpoint.getPosition()) {
                checkpoint1.setPosition(checkpoint1.getPosition() - 1);
              }
            })
            .collect(Collectors.toList())
        );
        Bukkit.getPluginManager().callEvent(new DeleteRaceCheckpointEvent(race, checkpoint));
        runnable.run();
      }
    }));
  }

  public void createRace(Race race, Runnable callback) {
    api.updateRace(race, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        racesByName.put(race.getName(), race);
        races.add(race);
        Bukkit.getPluginManager().callEvent(new CreateRaceEvent(race));
        callback.run();
      }
    }));
  }

  public void deleteRace(Race race, Runnable runnable) {
    api.deleteRace(race, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        racesByName.remove(race.getName());
        races.remove(race);
        Bukkit.getPluginManager().callEvent(new DeleteRaceEvent(race));
        runnable.run();
      }
    }));
  }

  public void addStartPoint(Location location, Race race, Consumer<RaceStartPoint> consumer) {
    RaceStartPoint startPoint = new RaceStartPoint(UUID.randomUUID(), race.getStartPoints().size() + 1, location);

    api.addStartPoint(race.getId(), startPoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.addStartPoint(startPoint);
        Bukkit.getPluginManager().callEvent(new AddRaceStartPointEvent(race, startPoint));
        consumer.accept(startPoint);
      }
    }));
  }

  public void deleteStartPoint(Race race, RaceStartPoint startPoint, Runnable runnable) {
    api.deleteStartPoint(race.getId(), startPoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.setStartPoints(
          race.
            getStartPoints()
            .stream()
            .filter((RaceStartPoint startPoint1) -> startPoint1 != startPoint)
            .peek((RaceStartPoint startPoint1) -> {
              if(startPoint1.getPosition() > startPoint.getPosition()) {
                startPoint1.setPosition(startPoint1.getPosition() - 1);
              }
            })
            .collect(Collectors.toList())
        );
        Bukkit.getPluginManager().callEvent(new DeleteRaceStartPointEvent(race, startPoint));
        runnable.run();
      }
    }));
  }

  public Race getRace(String name) {
    return racesByName.get(name);
  }

  public List<Race> getRaces() {
    return new ArrayList<>(races);
  }

  public boolean hasRace(String name) {
    return racesByName.containsKey(name);
  }
  
  public RaceSession getParticipatingRace(Player player) {
    for(RaceSession session : raceSessions) {
      if(session.isParticipating(player)) {
        return session;
      }
    }
    return null;
  }
}
