package com.github.hornta.race;

import com.github.hornta.race.api.RacingAPI;
import com.github.hornta.race.enums.RaceState;
import com.github.hornta.race.enums.RacingType;
import com.github.hornta.race.events.AddRaceCheckpointEvent;
import com.github.hornta.race.events.AddRaceStartPointEvent;
import com.github.hornta.race.events.ChangeRaceNameEvent;
import com.github.hornta.race.events.CreateRaceEvent;
import com.github.hornta.race.events.DeleteRaceCheckpointEvent;
import com.github.hornta.race.events.DeleteRaceEvent;
import com.github.hornta.race.events.DeleteRaceStartPointEvent;
import com.github.hornta.race.events.EditingRaceEvent;
import com.github.hornta.race.events.RaceSessionResultEvent;
import com.github.hornta.race.events.RaceSessionStopEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.PlayerSessionResult;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceCheckpoint;
import com.github.hornta.race.objects.RacePlayerSession;
import com.github.hornta.race.objects.RacePoint;
import com.github.hornta.race.objects.RaceSession;
import com.github.hornta.race.objects.RaceStartPoint;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RacingManager implements Listener {
  private static final Vector HologramOffset = new Vector(0, 1, 0);
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

  public void startNewSession(CommandSender initiator, Race race) {
    RaceSession raceSession = new RaceSession(initiator, race);
    raceSession.start();
    raceSessions.add(raceSession);
  }

  public List<RaceSession> getRaceSessions() {
    return new ArrayList<>(raceSessions);
  }

  public List<RaceSession> getRaceSessions(Race race) {
    return getRaceSessions(race, null);
  }

  public List<RaceSession> getRaceSessions(Race race, RaceState state) {
    List<RaceSession> sessions = new ArrayList<>();

    for(RaceSession session : raceSessions) {
      boolean stateOk = state == null || session.getState() == state;
      if(session.getRace() == race && stateOk) {
        sessions.add(session);
      }
    }

    return sessions;
  }

  @EventHandler
  void onAddRace(CreateRaceEvent event) {
    if(!event.getRace().isEditing()) {
      return;
    }

    for(RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      checkpoint.startTask(true);
      if(Racing.getInstance().isHolographicDisplaysLoaded()) {
        checkpoint.setHologram(getRacePointHologram(checkpoint));
      }
    }

    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
        startPoint.setHologram(getRacePointHologram(startPoint));
      }
    }
  }

  @EventHandler
  void onDeleteRace(DeleteRaceEvent event) {
    for(RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      checkpoint.stopTask();

      if(Racing.getInstance().isHolographicDisplaysLoaded()) {
        checkpoint.getHologram().delete();
        checkpoint.setHologram(null);
      }
    }

    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
        if(startPoint.getHologram() != null) {
          startPoint.getHologram().delete();
          startPoint.setHologram(null);
        }
      }
    }
  }

  @EventHandler
  void onAddRaceCheckpoint(AddRaceCheckpointEvent event) {
    event.getCheckpoint().startTask(true);
    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      event.getCheckpoint().setHologram(getRacePointHologram(event.getCheckpoint()));
    }
  }

  @EventHandler
  void onDeleteRaceCheckpoint(DeleteRaceCheckpointEvent event) {
    event.getCheckpoint().stopTask();

    if(Racing.getInstance().isHolographicDisplaysLoaded() && event.getCheckpoint().getHologram() != null) {
      event.getCheckpoint().getHologram().delete();
      event.getCheckpoint().setHologram(null);
    }
  }

  @EventHandler
  void onAddRaceStartPoint(AddRaceStartPointEvent event) {
    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      event.getStartPoint().setHologram(getRacePointHologram(event.getStartPoint()));
    }
  }

  @EventHandler
  void onDeleteRaceStartPoint(DeleteRaceStartPointEvent event) {
    if(!Racing.getInstance().isHolographicDisplaysLoaded()) {
      return;
    }

    if(event.getStartPoint().getHologram() != null) {
      event.getStartPoint().getHologram().delete();
      event.getStartPoint().setHologram(null);
    }

    for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
      if (startPoint.getPosition() > event.getStartPoint().getPosition() && startPoint.getHologram() != null) {
        startPoint.getHologram().delete();
        startPoint.setHologram(getRacePointHologram(startPoint));
      }
    }
  }

  @EventHandler
  void onEditingRace(EditingRaceEvent event) {
    for (RaceCheckpoint checkpoint : event.getRace().getCheckpoints()) {
      if (event.isEditing()) {
        checkpoint.startTask(true);
        if(Racing.getInstance().isHolographicDisplaysLoaded()) {
          checkpoint.setHologram(getRacePointHologram(checkpoint));
        }
      } else {
        checkpoint.stopTask();
        if(Racing.getInstance().isHolographicDisplaysLoaded()) {
          checkpoint.getHologram().delete();
          checkpoint.setHologram(null);
        }
      }
    }

    if(Racing.getInstance().isHolographicDisplaysLoaded()) {
      for (RaceStartPoint startPoint : event.getRace().getStartPoints()) {
        if (event.isEditing()) {
          startPoint.setHologram(getRacePointHologram(startPoint));
        } else {
          startPoint.getHologram().delete();
          startPoint.setHologram(null);
        }
      }
    }
  }

  @EventHandler
  void onChangeRaceName(ChangeRaceNameEvent event) {
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

    api.addStartPoint(race, checkpoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.addStartPoint(checkpoint);
        Bukkit.getPluginManager().callEvent(new AddRaceCheckpointEvent(race, checkpoint));
        consumer.accept(checkpoint);
      }
    }));
  }

  public void deleteCheckpoint(Race race, RaceCheckpoint checkpoint, Runnable runnable) {
    api.deletePoint(race, checkpoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.delPoint(checkpoint);
        Bukkit.getPluginManager().callEvent(new DeleteRaceCheckpointEvent(race, checkpoint));
        runnable.run();
      }
    }));
  }

  public void createRace(Location location, String name, Consumer<Race> consumer) {
    Race race = new Race(
      UUID.randomUUID(),
      Racing.getInstance().getDescription().getVersion(),
      name,
      location,
      false,
      true,
      Instant.now(),
      Collections.emptyList(),
      Collections.emptyList(),
      RacingType.PLAYER,
      null);

    api.createRace(race, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        racesByName.put(name, race);
        races.add(race);
        Bukkit.getPluginManager().callEvent(new CreateRaceEvent(race));
        consumer.accept(race);
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

    api.addRaceStart(race, startPoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.addStartPoint(startPoint);
        Bukkit.getPluginManager().callEvent(new AddRaceStartPointEvent(race, startPoint));
        consumer.accept(startPoint);
      }
    }));
  }

  public void deleteStartPoint(Race race, RaceStartPoint startPoint, Runnable runnable) {
    api.deleteStartPoint(race, startPoint, (Boolean result) -> Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
      if(result) {
        race.deleteStartPoint(startPoint);
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

  private Hologram getRacePointHologram(RacePoint startPoint) {
    Hologram hologram = HologramsAPI.createHologram(Racing.getInstance(), startPoint.getLocation().add(HologramOffset));
    hologram.appendTextLine("§d" + startPoint.getPosition());
    hologram.getVisibilityManager().setVisibleByDefault(false);
    return hologram;
  }
}
