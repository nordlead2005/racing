package com.github.hornta.race;

import com.github.hornta.race.enums.Permission;
import com.github.hornta.race.enums.RaceSessionState;
import com.github.hornta.race.events.*;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.Race;
import com.github.hornta.race.objects.RaceSession;
import com.github.hornta.race.objects.RaceSign;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class SignManager implements Listener {
  private RacingManager racingManager;
  private HashMap<RaceSign, Race> racesBySign = new HashMap<>();
  private HashMap<String, RaceSign> raceSigns = new HashMap<>();
  private static Set<Material> wallSignMaterials;
  private static Set<Material> signPostMaterial;

  static {
    wallSignMaterials = new HashSet<>();
    wallSignMaterials.add(Material.SPRUCE_WALL_SIGN);
    wallSignMaterials.add(Material.ACACIA_WALL_SIGN);
    wallSignMaterials.add(Material.BIRCH_WALL_SIGN);
    wallSignMaterials.add(Material.DARK_OAK_WALL_SIGN);
    wallSignMaterials.add(Material.JUNGLE_WALL_SIGN);
    wallSignMaterials.add(Material.OAK_WALL_SIGN);
    wallSignMaterials.add(Material.LEGACY_WALL_SIGN);

    signPostMaterial = new HashSet<>();
    signPostMaterial.add(Material.SPRUCE_SIGN);
    signPostMaterial.add(Material.ACACIA_SIGN);
    signPostMaterial.add(Material.BIRCH_SIGN);
    signPostMaterial.add(Material.DARK_OAK_SIGN);
    signPostMaterial.add(Material.JUNGLE_SIGN);
    signPostMaterial.add(Material.OAK_SIGN);
    signPostMaterial.add(Material.LEGACY_SIGN);
  }

  SignManager(RacingManager racingManager) {
    this.racingManager = racingManager;
  }

  @EventHandler
  void onCreateRace(CreateRaceEvent event) {
    for(RaceSign sign : event.getRace().getSigns()) {
      raceSigns.put(sign.getKey(), sign);
      racesBySign.put(sign, event.getRace());
    }
  }

  @EventHandler
  void onDeleteRace(DeleteRaceEvent event) {
    for(RaceSign sign : event.getRace().getSigns()) {
      raceSigns.remove(sign.getKey());
      racesBySign.remove(sign);
    }
  }

  @EventHandler
  void onRaceChangeName(RaceChangeNameEvent event) {
    event.getRace().getSigns().stream().forEach(this::updateSign);
  }

  @EventHandler
  void onParticipate(ParticipateEvent event) {
    event.getRaceSession().getRace().getSigns().stream().forEach(this::updateSign);
  }

  @EventHandler
  void onSessionStateChanged(SessionStateChangedEvent event) {
    event.getRaceSession().getRace().getSigns().stream().forEach(this::updateSign);
  }

  @EventHandler
  void onRaceSessionStop(RaceSessionStopEvent event) {
    event.getRaceSession().getRace().getSigns().stream().forEach(this::updateSign);
  }

  @EventHandler
  void onLeave(LeaveEvent event) {
    event.getRaceSession().getRace().getSigns().stream().forEach(this::updateSign);
  }

  @EventHandler
  void onSignChange(SignChangeEvent event) {
    Race race = racingManager.getRace(event.getLine(1));
    if(race == null) {
      return;
    }

    boolean creatingRaceSign =
      event.getLine(0).equalsIgnoreCase("race") &&
      event.getPlayer().hasPermission(Permission.RACING_MODIFY.toString());

    if (!creatingRaceSign) {
      return;
    }

    Sign sign = (Sign)event.getBlock().getState();
    RaceSign raceSign = new RaceSign(sign, event.getPlayer().getUniqueId(), Instant.now());
    race.getSigns().add(raceSign);
    racingManager.updateRace(race, () -> {
      raceSigns.put(raceSign.getKey(), raceSign);
      racesBySign.put(raceSign, race);
      updateSign(raceSign);
      MessageManager.setValue("race_name", race.getName());
      MessageManager.sendMessage(event.getPlayer(), MessageKey.SIGN_REGISTERED);
    });
  }

  @EventHandler
  void onBlockPhysics(BlockPhysicsEvent event) {
    boolean isSign = signPostMaterial.contains(event.getBlock().getType());
    boolean isWallSign = wallSignMaterials.contains(event.getBlock().getType());

    if (!isSign && !isWallSign) {
      return;
    }

    WallSign wallSign;
    org.bukkit.block.data.type.Sign signPost;
    Block parent;

    if(isSign) {
      signPost = (org.bukkit.block.data.type.Sign) event.getBlock().getBlockData();
      parent = event.getBlock().getRelative(BlockFace.DOWN);
    } else {
      wallSign = (WallSign) event.getBlock().getBlockData();
      parent = event.getBlock().getRelative(wallSign.getFacing().getOppositeFace());
    }

    if (parent.getType() == Material.AIR) {
      removeSign(event, null);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onBlockBreak(BlockBreakEvent event) {
    if(event.isCancelled()) {
      return;
    }
    removeSign(event, event.getPlayer());
  }

  private void removeSign(BlockEvent event, Player player) {
    RaceSign sign = getRaceSignFromBlock(event.getBlock());
    if(sign == null) {
      return;
    }

    Race race = racesBySign.get(sign);
    if (race == null) {
      return;
    }

    race.getSigns().remove(sign);
    racingManager.updateRace(racesBySign.get(sign), () -> {
      raceSigns.remove(sign.getKey());
      racesBySign.remove(sign);
      MessageManager.setValue("race_name", race.getName());
      if(player != null) {
        MessageManager.sendMessage(player, MessageKey.SIGN_UNREGISTERED);
      } else {
        Racing.logger().log(Level.INFO, MessageManager.getMessage(MessageKey.SIGN_UNREGISTERED));
      }
    });
  }

  @EventHandler
  void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getClickedBlock() == null) {
      return;
    }

    RaceSign sign = getRaceSignFromBlock(event.getClickedBlock());
    if(sign == null) {
      return;
    }

    if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      racingManager.joinRace(racesBySign.get(sign), event.getPlayer());
    }
  }

  private RaceSign getRaceSignFromBlock(Block block) {
    if(!wallSignMaterials.contains(block.getType()) && !signPostMaterial.contains(block.getType())) {
      return null;
    }

    return raceSigns.get(RaceSign.createKey(block));
  }

  private void updateSign(RaceSign sign) {
    Race race = racesBySign.get(sign);
    List<RaceSession> sessions = racingManager.getRaceSessions(race);
    RaceSession session = sessions.isEmpty() ? null : sessions.get(0);

    MessageKey statusKey;
    if(session == null) {
      statusKey = MessageKey.SIGN_NOT_STARTED;
    } else if (session.getState() == RaceSessionState.PREPARING) {
      statusKey = MessageKey.SIGN_LOBBY;
    } else {
      statusKey = MessageKey.SIGN_STARTED;
    }
    MessageManager.setValue("status", MessageManager.getMessage(statusKey));
    MessageManager.setValue("race_name", race.getName());
    MessageManager.setValue("current_participants", session == null ? 0 : session.getAmountOfParticipants());
    MessageManager.setValue("max_participants", race.getStartPoints().size());
    String[] contents = MessageManager.getMessage(MessageKey.RACE_SIGN_LINES).split("\n");
    for(int i = 0; i < contents.length; ++i) {
      sign.getSign().setLine(i, contents[i]);
    }
    sign.getSign().update();
  }
}
