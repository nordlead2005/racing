package com.github.hornta.race.objects;

import com.github.hornta.race.Racing;
import com.github.hornta.race.SongManager;
import com.github.hornta.race.Util;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.enums.RaceType;
import com.github.hornta.race.events.RacePlayerGoalEvent;
import com.github.hornta.race.events.RaceSessionResultEvent;
import com.github.hornta.race.events.RaceSessionStopEvent;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class RaceSession implements Listener {
  private final UUID id;
  private final CommandSender initiator;
  private final Race race;
  private final int laps;

  private RadioSongPlayer songPlayer;
  private RaceSessionState state;
  private List<BukkitTask> startTimerTasks = new ArrayList<>();
  private RaceCountdown countdown;
  private Instant start;
  private int numFinished;
  private Map<UUID, RacePlayerSession> playerSessions = new HashMap<>();
  private Team team;
  private RaceSessionResult result;

  public RaceSession(CommandSender initiator, Race race, int laps) {
    this.race = race;
    this.initiator = initiator;
    this.laps = laps;

    id = UUID.randomUUID();

    if(Racing.getInstance().isNoteBlockAPILoaded() && race.getSong() != null) {
      songPlayer = new RadioSongPlayer(SongManager.getSongByName(race.getSong()));
    }
  }

  public RaceSessionState getState() {
    return state;
  }

  public void setState(RaceSessionState state) {
    this.state = state;
  }

  public Race getRace() {
    return race;
  }

  public void start() {
    result = new RaceSessionResult(this);
    Bukkit.getServer().getPluginManager().registerEvents(this, Racing.getInstance());

    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageManager.getMessage(MessageKey.PARTICIPATE_HOVER_TEXT)).create());

    MessageManager.setValue("race_name", race.getName());
    ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageManager.getMessage(MessageKey.PARTICIPATE_CLICK_TEXT));

    int prepareTime = RaceConfiguration.getValue(ConfigKey.RACE_PREPARE_TIME);

    MessageManager.setValue("command_sender", initiator.getName());
    MessageManager.setValue("race_name", race.getName());
    MessageManager.setValue("time_left", Util.getTimeLeft(prepareTime));
    MessageManager.setValue("laps", laps);

    MessageKey key = MessageKey.PARTICIPATE_TEXT;
    Economy economy = Racing.getInstance().getEconomy();

    if(economy != null) {
      key = MessageKey.PARTICIPATE_TEXT_FEE;
      MessageManager.setValue("entry_fee", economy.format(race.getEntryFee()));
    }

    ComponentBuilder announceMessage = new ComponentBuilder("").event(hoverEvent).event(clickEvent);

    Util.setTimeUnitValues();
    Bukkit.getServer().spigot().broadcast(
      new ComponentBuilder(announceMessage).append(
        TextComponent.fromLegacyText(MessageManager.getMessage(key))
      ).create()
    );

    setState(RaceSessionState.PREPARING);

    MessageManager.setValue("race_name", race.getName());
    ClickEvent skipWaitClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageManager.getMessage(MessageKey.SKIP_WAIT_CLICK_TEXT));
    MessageManager.setValue("race_name", race.getName());
    ClickEvent stopClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageManager.getMessage(MessageKey.STOP_RACE_CLICK_TEXT));

    HoverEvent skipWaitHover = new HoverEvent(
      HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageManager.getMessage(MessageKey.SKIP_WAIT_HOVER_TEXT)).create()
    );
    HoverEvent stopHover = new HoverEvent(
      HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MessageManager.getMessage(MessageKey.STOP_RACE_HOVER_TEXT)).create()
    );

    TextComponent tc = new TextComponent();
    tc.addExtra(
      new ComponentBuilder(MessageManager.getMessage(MessageKey.SKIP_WAIT))
      .event(skipWaitHover)
      .event(skipWaitClickEvent).create()[0]
    );
    tc.addExtra(" ");
    tc.addExtra(
      new ComponentBuilder(MessageManager.getMessage(MessageKey.STOP_RACE))
      .event(stopHover)
      .event(stopClickEvent)
      .create()[0]
    );

    initiator.spigot().sendMessage(tc);
    
    List<Integer> announceIntervals = RaceConfiguration.getValue(ConfigKey.RACE_ANNOUNCE_INTERVALS);
    for(int interval : announceIntervals) {
      if(interval >= prepareTime) {
        return;
      }
      addStartTimerTask(Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), () -> {
        MessageManager.setValue("race_name", race.getName());
        MessageManager.setValue("time_left", Util.getTimeLeft(interval));
        Util.setTimeUnitValues();
        Bukkit.getServer().spigot().broadcast(
          new ComponentBuilder(announceMessage).append(
            TextComponent.fromLegacyText(MessageManager.getMessage(MessageKey.PARTICIPATE_TEXT_TIMELEFT))
          ).create()
        );
      }, (long)(prepareTime - interval) * 20));
    }

    addStartTimerTask(Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), this::actualStart, prepareTime * 20));
  }

  private void actualStart() {
    // check if players are online before countdown starts
    for(RacePlayerSession session : playerSessions.values()) {
      if(session.getPlayer() == null) {
        playerSessions.remove(session.getPlayerId());
        for(RacePlayerSession session1 : playerSessions.values()) {
          MessageManager.setValue("player_name", session.getPlayerName());
          MessageManager.sendMessage(session1.getPlayer(), MessageKey.NOSHOW_DISQUALIFIED);
        }

        Economy economy = Racing.getInstance().getEconomy();
        if (economy != null && session.getChargedEntryFee() > 0) {
          economy.depositPlayer(Bukkit.getOfflinePlayer(session.getPlayerId()), session.getChargedEntryFee());
        }
      }
    }

    if(playerSessions.isEmpty()) {
      MessageManager.broadcast(MessageKey.RACE_CANCELED);
      stop();
      return;
    }

    setState(RaceSessionState.COUNTDOWN);

    List<RacePlayerSession> shuffledSessions = new ArrayList<>(playerSessions.values());
    Collections.shuffle(shuffledSessions);

    for(RaceCheckpoint checkpoint : race.getCheckpoints()) {
      checkpoint.startTask();
    }

    int startPointIndex = 0;
    for(RacePlayerSession session : shuffledSessions) {
      session.setCurrentLap(1);
      session.setStartPoint(race.getStartPoints().get(startPointIndex));
      session.setBossBar(Bukkit.createBossBar(getBossBarTitle(session), BarColor.BLUE, BarStyle.SOLID));
      session.startCooldown();
      tryIncrementCheckpoint(session);
      startPointIndex += 1;
    }

    countdown = new RaceCountdown(playerSessions.values());
    countdown.start(() -> {
      setState(RaceSessionState.STARTED);
      String teamName = id.toString().substring(0, 15);
      team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(teamName);
      if(team == null) {
        team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam(teamName);
      }
      team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

      List<PotionEffect> potionEffects = new ArrayList<>();
      for(RacePotionEffect racePotionEffect : race.getPotionEffects()) {
        potionEffects.add(
          new PotionEffect(
            racePotionEffect.getType(),
            Integer.MAX_VALUE,
            racePotionEffect.getAmplifier(),
            false,
            false,
            false
          )
        );
      }

      for(RacePlayerSession session : playerSessions.values()) {
        session.startRace();

        for(PotionEffect potionEffect : potionEffects) {
          session.getPlayer().addPotionEffect(potionEffect);
        }

        if(songPlayer != null) {
          songPlayer.addPlayer(session.getPlayer());
        }
        team.addEntry(session.getPlayer().getName());
      }

      if(songPlayer != null) {
        songPlayer.setTick((short) 0);
        songPlayer.setPlaying(true);
      }
      start = Instant.now();
    });
  }

  public void skipToCountdown() {
    for(BukkitTask task : startTimerTasks) {
      task.cancel();
    }

    if(state != RaceSessionState.COUNTDOWN) {
      actualStart();
    }
  }

  public void stop() {
    if(countdown != null) {
      countdown.stop();
      countdown = null;
    }

    for(BukkitTask task : startTimerTasks) {
      task.cancel();
    }
    startTimerTasks.clear();

    for (RacePlayerSession session : playerSessions.values()) {
      session.restore();
      if(songPlayer != null) {
        songPlayer.removePlayer(session.getPlayer());
      }
    }

    if(songPlayer != null) {
      songPlayer.setPlaying(false);
    }

    playerSessions.clear();

    if(state != RaceSessionState.PREPARING) {
      for (RaceCheckpoint checkpoint : race.getCheckpoints()) {
        checkpoint.stopTask();
      }
    }

    numFinished = 0;

    if(team != null) {
      for(String entry : team.getEntries()) {
        team.removeEntry(entry);
      }
      team.unregister();
      team = null;
    }

    HandlerList.unregisterAll(this);

    Bukkit.getPluginManager().callEvent(new RaceSessionStopEvent(this));
  }

  public void leave(Player player) {
    RacePlayerSession playerSession = playerSessions.get(player.getUniqueId());
    playerSession.restore();
    playerSessions.remove(player.getUniqueId());

    Economy economy = Racing.getInstance().getEconomy();
    if(economy != null && playerSession.getChargedEntryFee() > 0) {
      economy.depositPlayer(player, playerSession.getChargedEntryFee());
      MessageManager.setValue("entry_fee", economy.format(playerSession.getChargedEntryFee()));
      MessageManager.sendMessage(player, MessageKey.RACE_LEAVE_PAYBACK);
    }

    for(RacePlayerSession session : playerSessions.values()) {
      if(session.getPlayer() != null) {
        MessageManager.setValue("player_name", player.getName());
        MessageManager.setValue("race_name", race.getName());
        MessageManager.sendMessage(session.getPlayer(), MessageKey.RACE_LEAVE_BROADCAST);
      }
    }

    if(playerSessions.isEmpty() && (state == RaceSessionState.COUNTDOWN || state == RaceSessionState.STARTED)) {
      stop();
    }
  }

  public boolean isFull() {
    return playerSessions.size() == race.getStartPoints().size();
  }

  public boolean isParticipating(Player player) {
    return playerSessions.containsKey(player.getUniqueId());
  }

  public int getAmountOfParticipants() {
    return playerSessions.size();
  }

  public void participate(Player player, double chargedEntryFee) {
    playerSessions.put(player.getUniqueId(), new RacePlayerSession(race, player, chargedEntryFee));
  }

  private void addStartTimerTask(int id) {
    startTimerTasks.add(Bukkit.getScheduler().getPendingTasks().stream().filter(t -> t.getTaskId() == id).findFirst().get());
  }

  private void tryIncrementCheckpoint(RacePlayerSession playerSession) {
    RaceCheckpoint nextCheckpoint = playerSession.getNextCheckpoint();
    boolean hasFinished = playerSession.getCurrentCheckpoint() != null && nextCheckpoint == null;
    if(hasFinished) {
      return;
    }

    if(nextCheckpoint == null) {
      playerSession.setNextCheckpoint(race.getCheckpoint(1));
      playerSession.getBossBar().setProgress(0);
    } else {
      int numCheckpoints = race.getCheckpoints().size();
      Player player = playerSession.getPlayer();

      if(nextCheckpoint.isInside(player)) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        int checkpointIndex = race.getCheckpoints().indexOf(nextCheckpoint);
        int totalCheckpoints = numCheckpoints * laps;
        int currentCheckpoints = (playerSession.getCurrentLap() - 1) * numCheckpoints + checkpointIndex + 1;
        double progress = currentCheckpoints / (double)totalCheckpoints;
        playerSession.getBossBar().setProgress(progress);

        boolean isLastCheckpoint = checkpointIndex == numCheckpoints - 1;

        if(isLastCheckpoint && playerSession.getCurrentLap() == laps) {
          playerSession.setNextCheckpoint(null);
          Bukkit.getPluginManager().callEvent(new RacePlayerGoalEvent(this, playerSession));
        } else {
          if(isLastCheckpoint) {
            playerSession.setNextCheckpoint(race.getCheckpoint(1));
            playerSession.setCurrentLap(playerSession.getCurrentLap() + 1);
            playerSession.getBossBar().setTitle(getBossBarTitle(playerSession));

            if(laps > 1) {
              String message;
              if (playerSession.getCurrentLap() == laps) {
                message = MessageManager.getMessage(MessageKey.RACE_FINAL_LAP);
              } else {
                MessageManager.setValue("ordinal", playerSession.getCurrentLap());
                message = MessageManager.getMessage(MessageKey.RACE_NEXT_LAP);
              }
              player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
          } else {
            playerSession.setNextCheckpoint(race.getCheckpoint(nextCheckpoint.getPosition() + 1));
          }
        }
      }
    }
  }

  @EventHandler
  void onPlayerTeleport(PlayerTeleportEvent event) {
    if(isParticipating(event.getPlayer()) && (
      event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
        event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
    )) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onVehicleMove(VehicleMoveEvent event) {
    if (
      race.getType() != RaceType.MINECART ||
      event.getVehicle().getPassengers().isEmpty() ||
      !(event.getVehicle().getPassengers().get(0) instanceof Player)
    ) {
      return;
    }

    Player player = (Player) event.getVehicle().getPassengers().get(0);
    if(!isParticipating(player)) {
      return;
    }

    RacePlayerSession playerSession = playerSessions.get(player.getUniqueId());

    if(state == RaceSessionState.COUNTDOWN || state == RaceSessionState.STARTED) {
      tryIncrementCheckpoint(playerSession);
    }

    if(state != RaceSessionState.COUNTDOWN) {
      return;
    }

    if(playerSession.getStartLocation().distanceSquared(event.getTo()) >= 1) {
      playerSession.respawnInVehicle();
    }
  }

  @EventHandler
  void onPlayerMove(PlayerMoveEvent event) {
    if(isParticipating(event.getPlayer()) && (state == RaceSessionState.COUNTDOWN || state == RaceSessionState.STARTED)) {
      RacePlayerSession playerSession = playerSessions.get(event.getPlayer().getUniqueId());

      tryIncrementCheckpoint(playerSession);

      // prevent player from moving after being teleported to the start point
      // will happen when player for example is holding walk forward button while being teleported

      if(state != RaceSessionState.COUNTDOWN) {
        return;
      }

      if(
        playerSession.getHorse() != null ||
        playerSession.getBoat() != null ||
        playerSession.getMinecart() != null
      ) {
        if(playerSession.getStartLocation().distanceSquared(event.getTo()) >= 1) {
          playerSession.respawnInVehicle();
        }
      } else {
        if(
          Double.compare(event.getFrom().getX(), event.getTo().getX()) == 0 &&
            Double.compare(event.getFrom().getY(), event.getTo().getY()) == 0 &&
            Double.compare(event.getFrom().getZ(), event.getTo().getZ()) == 0
        ) {
          return;
        }

        event.setTo(new Location(
          event.getFrom().getWorld(),
          event.getFrom().getX(),
          event.getFrom().getY(),
          event.getFrom().getZ(),
          event.getTo().getYaw(),
          event.getTo().getPitch()
        ));
      }
    }
  }

  @EventHandler
  void onFoodLevelChange(FoodLevelChangeEvent event) {
    if(event.getEntityType() == EntityType.PLAYER && isParticipating((Player)event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onEntityTarget(EntityTargetEvent event) {
    if((event.getTarget() instanceof Player) && isParticipating((Player) event.getTarget()) && state == RaceSessionState.COUNTDOWN) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onPlayerKick(PlayerKickEvent event) {
    Player player = event.getPlayer();
    if(isParticipating(player) && (state == RaceSessionState.COUNTDOWN || state == RaceSessionState.STARTED)) {
      playerSessions.get(player.getUniqueId()).restore();
    }
  }

  @EventHandler
  void onPlayerJoin(PlayerJoinEvent event) {
    if(isParticipating(event.getPlayer()) && state == RaceSessionState.PREPARING) {
     RacePlayerSession playerSession = playerSessions.get(event.getPlayer().getUniqueId());
     playerSession.setPlayer(event.getPlayer());
    }
  }

  @EventHandler
  void onPlayerQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();

    if(!isParticipating(player)) {
      return;
    }

    RacePlayerSession playerSession = playerSessions.get(player.getUniqueId());

    if (state == RaceSessionState.PREPARING) {
      playerSession.setPlayer(null);
      return;
    }

    playerSession.restore();
    playerSessions.remove(player.getUniqueId());

    for(RacePlayerSession session : playerSessions.values()) {
      MessageManager.setValue("player_name", player.getName());
      MessageManager.sendMessage(session.getPlayer(), MessageKey.QUIT_DISQULIAFIED);
    }

    if(playerSessions.isEmpty()) {
      stop();
    }
  }

  @EventHandler
  void onEntityDamage(EntityDamageEvent event) {
    if(!(event.getEntity() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntity();

    if(!isParticipating(player)) {
      return;
    }

    if(event.getFinalDamage() >= player.getHealth()) {
      playerSessions.get(player.getUniqueId()).respawnOnDeath(event);
    }
  }

  @EventHandler
  void onVehicleEnter(VehicleEnterEvent event) {
    if(!(event.getEntered() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getEntered();

    if(!isParticipating(player)) {
      return;
    }

    RacePlayerSession session = playerSessions.get(player.getUniqueId());

    // if player is already mounted we need to cancel a new attempt to mount
    if(!session.isAllowedToEnterVehicle()) {
      event.setCancelled(true);

      // because the player attempted to mount another vehicle, they become automatically dismounted from their current vehicle
      if(session.getVehicle() != event.getVehicle()) {
        // remount them onto their real vehicle
        Bukkit.getScheduler().scheduleSyncDelayedTask(Racing.getInstance(), session::enterVehicle);
      }

      return;
    }

    if(race.getType() == RaceType.PIG && event.getVehicle().getType() != EntityType.PIG) {
      event.setCancelled(true);
    }

    if(race.getType() == RaceType.HORSE && event.getVehicle().getType() != EntityType.HORSE) {
      event.setCancelled(true);
    }

    if(race.getType() == RaceType.MINECART && event.getVehicle().getType() != EntityType.MINECART) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onVehicleExit(VehicleExitEvent event) {
    if(!(event.getExited() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getExited();

    if(!isParticipating(player)) {
      return;
    }

    if(!playerSessions.get(player.getUniqueId()).isAllowedToExitVehicle()) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  void onRacePlayerGoal(RacePlayerGoalEvent event) {
    numFinished += 1;
    result.addPlayerRessionResult(event.getPlayerSession(), numFinished, Duration.between(start, Instant.now()));

    if(numFinished == playerSessions.size()) {
      stop();
      Bukkit.getPluginManager().callEvent(new RaceSessionResultEvent(result));
    }
  }

  @EventHandler
  void onPlayerDropItem(PlayerDropItemEvent event) {
    if(!isParticipating(event.getPlayer())) {
      return;
    }

    switch (race.getType()) {
      case ELYTRA:
        if(event.getItemDrop().getItemStack().getType() == Material.ELYTRA) {
          event.setCancelled(true);
        }
        break;

      case PIG:
        if(event.getItemDrop().getItemStack().getType() == Material.CARROT_ON_A_STICK) {
          event.setCancelled(true);
        }
        break;

      case HORSE:
        if(event.getItemDrop().getItemStack().getType() == Material.SADDLE) {
          event.setCancelled(true);
        }
        break;

      default:
    }
  }

  @EventHandler
  void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
    if(!isParticipating(event.getPlayer())) {
      return;
    }

    switch (race.getType()) {
      case PIG:
        if(event.getItem().getType() == Material.CARROT_ON_A_STICK) {
          event.setCancelled(true);
        }
        break;

      case ELYTRA:
        if(event.getItem().getType() == Material.ELYTRA) {
          event.setCancelled(true);
        }
        break;

      default:
    }
  }

  @EventHandler
  void onPlayerInteract(PlayerInteractEvent event) {
    if(
      !isParticipating(event.getPlayer()) ||
      state != RaceSessionState.STARTED
    ) {
      return;
    }

    if(race.getType() != RaceType.ELYTRA && race.getType() != RaceType.MINECART) {
      return;
    }

    RacePlayerSession playerSession = playerSessions.get(event.getPlayer().getUniqueId());
    // set fall distance to zero so players doesn't take damage when they are falling
    playerSession.getPlayer().setFallDistance(0);

    if(race.getType() == RaceType.MINECART) {
      playerSession.respawnInVehicle();
    } else if(race.getType() == RaceType.ELYTRA) {
      playerSession.getPlayer().teleport(playerSession.getStartLocation());
    }
  }

  private String getBossBarTitle(RacePlayerSession session) {
    if(laps == 1) {
      return race.getName();
    }

    return race.getName() + " lap " + session.getCurrentLap() + "/" + laps;
  }
}


