package com.github.hornta.race;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.hornta.*;
import com.github.hornta.race.api.FileAPI;
import com.github.hornta.race.api.StorageType;
import com.github.hornta.race.commands.*;
import com.github.hornta.race.commands.completers.*;
import com.github.hornta.race.commands.validators.*;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.mcmmo.McMMOListener;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.message.Translations;
import com.gmail.nossr50.mcMMO;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Racing extends JavaPlugin {
  private static Racing instance;
  private boolean isNoteBlockAPILoaded;
  private boolean isHolographicDisplaysLoaded;
  private boolean isVaultLoaded;
  private Economy economy;
  private Carbon carbon;
  private Translations translations;
  private RacingManager racingManager;
  private ProtocolManager protocolManager;

  public static Racing getInstance() {
    return instance;
  }

  public static Logger logger() {
    return instance.getLogger();
  }

  public ProtocolManager getProtocolManager() {
    return protocolManager;
  }

  public Economy getEconomy() {
    return economy;
  }

  @Override
  public void onLoad() {
    protocolManager = ProtocolLibrary.getProtocolManager();
  }

  public boolean isNoteBlockAPILoaded() {
    return isNoteBlockAPILoaded;
  }

  public boolean isHolographicDisplaysLoaded() {
    return isHolographicDisplaysLoaded;
  }

  public Carbon getCarbon() {
    return carbon;
  }

  private void setupCommands() {
    RaceCompleter raceCompleter = new RaceCompleter(racingManager);
    PointExistValidator checkpointShouldExist = new PointExistValidator(racingManager, true);
    PointCompleter checkpointCompleter = new PointCompleter(racingManager);
    StartPointExistValidator startPointShouldExist = new StartPointExistValidator(racingManager, true);
    StartPointCompleter startPointCompleter = new StartPointCompleter(racingManager);
    SongExistValidator songExistValidator = new SongExistValidator();
    SongCompleter songCompleter = new SongCompleter();

    carbon.setNoPermissionHandler((CommandSender commandSender, CarbonCommand command) -> {
      MessageManager.sendMessage(commandSender, MessageKey.NO_PERMISSION_COMMAND);
    });

    carbon.setMissingArgumentHandler((CommandSender commandSender, CarbonCommand command) -> {
      String helpTexts = String.join("\n", command.getHelpTexts().toArray(new String[0]));
      MessageManager.setValue("usage", helpTexts);
      MessageManager.sendMessage(commandSender, MessageKey.MISSING_ARGUMENTS_COMMAND);
    });

    carbon.setMissingCommandHandler((CommandSender sender, List<CarbonCommand> suggestions) -> {
      MessageManager.setValue("suggestions", suggestions.stream()
        .map(CarbonCommand::getHelpTexts)
        .flatMap(List::stream)
        .collect(Collectors.joining("\n")));
      MessageManager.sendMessage(sender, MessageKey.COMMAND_NOT_FOUND);
    });

    carbon.handleValidation((ValidationResult result) -> {
      switch (result.getStatus()) {
        case ERR_INCORRECT_TYPE:
          MessageManager.setValue("help_texts", result.getCommand().getHelpTexts().stream().collect(Collectors.joining("\n")));
          MessageManager.setValue("argument", result.getArgument().getName());
          MessageManager.setValue("received", result.getValue());
          if (result.getArgument().getType() == CarbonArgumentType.INTEGER) {
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_NON_INTEGER);
          } else if (result.getArgument().getType() == CarbonArgumentType.NUMBER) {
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_NON_INTEGER);
          }
          break;
        case ERR_MIN_LIMIT:
        case ERR_MAX_LIMIT:
          MessageManager.setValue("help_texts", result.getCommand().getHelpTexts().stream().collect(Collectors.joining("\n")));
          MessageManager.setValue("argument", result.getArgument().getName());
          MessageManager.setValue("received", result.getValue());
          if(result.getStatus() == ValidationStatus.ERR_MIN_LIMIT) {
            MessageManager.setValue("expected", result.getArgument().getMin());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_MIN_EXCEED);
          } else {
            MessageManager.setValue("expected", result.getArgument().getMax());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.VALIDATE_MAX_EXCEED);
          }
          break;
      }
    });

    CarbonArgument raceArgument = new CarbonArgument("race");
    raceArgument.ofType(CarbonArgumentType.OTHER);
    raceArgument.validate(new RaceExistValidator(racingManager, true));
    raceArgument.setTabCompleter(raceCompleter);

    carbon
      .addCommand("racing create")
      .withHandler(new CommandCreateRace(racingManager))
      .withArgument(
        new CarbonArgument("race")
        .ofType(CarbonArgumentType.OTHER)
        .validate(new RaceExistValidator(racingManager, false))
      )
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing delete")
      .withHandler(new CommandDeleteRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing list")
      .withHandler(new CommandRaces(racingManager))
      .requiresPermission(Permission.RACING_PLAYER.toString());

    carbon
      .addCommand("racing addcheckpoint")
      .withHandler(new CommandAddCheckpoint(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    CarbonArgument checkpointArgument = new CarbonArgument("point");
    checkpointArgument.validate(checkpointShouldExist);
    checkpointArgument.setTabCompleter(checkpointCompleter);
    checkpointArgument.dependsOn(raceArgument);

    carbon
      .addCommand("racing deletecheckpoint")
      .withHandler(new CommandDeleteCheckpoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(checkpointArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpcheckpoint")
      .withHandler(new CommandRaceTeleportPoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(checkpointArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing spawn")
      .withHandler(new CommandRaceSpawn(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing setspawn")
      .withHandler(new CommandRaceSetSpawn(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing setstate")
      .withHandler(new CommandSetRaceState(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument("state")
          .ofType(CarbonArgumentType.OTHER)
        .validate(new RaceStateValidator())
        .setTabCompleter(new RaceStateCompleter())
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing setname")
      .withHandler(new CommandSetRaceName(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument("name")
        .ofType(CarbonArgumentType.STRING)
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing settype")
      .withHandler(new CommandSetType(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument("type")
        .validate(new RaceTypeValidator())
        .setTabCompleter(new RaceTypeCompleter())
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    if(economy != null) {
      carbon
        .addCommand("racing setentryfee")
        .withHandler(new CommandSetEntryFee(racingManager))
        .withArgument(raceArgument)
        .withArgument(
          new CarbonArgument("fee")
            .ofType(CarbonArgumentType.NUMBER)
            .setMin(0)
        )
        .requiresPermission(Permission.RACING_MODIFY.toString());
    }

    carbon
      .addCommand("racing addstartpoint")
      .withHandler(new CommandAddStartpoint(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();


    CarbonArgument startPointArgument = new CarbonArgument("point");
    startPointArgument.validate(startPointShouldExist);
    startPointArgument.setTabCompleter(startPointCompleter);
    startPointArgument.dependsOn(raceArgument);

    carbon
      .addCommand("racing deletestartpoint")
      .withHandler(new CommandDeleteStartpoint(racingManager))
      .withArgument(raceArgument)
      .withArgument(startPointArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpstartpoint")
      .withHandler(new CommandRaceTeleportStart(racingManager))
      .withArgument(raceArgument)
      .withArgument(startPointArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    if(isNoteBlockAPILoaded) {
      CarbonArgument songArgument = new CarbonArgument("song");
      songArgument.validate(songExistValidator);
      songArgument.setTabCompleter(songCompleter);

      carbon
        .addCommand("racing setsong")
        .withHandler(new CommandSetSong(racingManager))
        .withArgument(raceArgument)
        .withArgument(songArgument)
        .requiresPermission(Permission.RACING_MODIFY.toString());

      carbon
        .addCommand("racing unsetsong")
        .withHandler(new CommandUnsetSong(racingManager))
        .withArgument(raceArgument)
        .requiresPermission(Permission.RACING_MODIFY.toString());

      carbon
        .addCommand("racing playsong")
        .withHandler(new CommandPlaySong())
        .withArgument(songArgument)
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();

      carbon
        .addCommand("racing stopsong")
        .withHandler(new CommandStopSong())
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();
    }

    carbon
      .addCommand("racing start")
      .withHandler(new CommandStartRace(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument("laps")
        .ofType(CarbonArgumentType.INTEGER)
        .defaultsTo(1)
        .setMin(1)
        .setOptional(true)
      )
      .requiresPermission(Permission.RACING_MODERATOR.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing join")
      .withHandler(new CommandJoinRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing stop")
      .withHandler(new CommandStopRace(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    carbon
      .addCommand("racing skipwait")
      .withHandler(new CommandSkipWait(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    carbon
      .addCommand("racing reload")
      .withHandler((CommandSender sender, String[] args) -> {
        RaceConfiguration.reload(this);
        SongManager.getInstance().loadSongs((String) RaceConfiguration.getValue(ConfigKey.SONGS_DIRECTORY));

        if(!racingManager.getRaceSessions().isEmpty()) {
          MessageManager.sendMessage(sender, MessageKey.RELOAD_NOT_RACES);
        } else {
          racingManager.load();
        }

        MessageManager.sendMessage(sender, MessageKey.RELOAD_SUCCESS);
      })
      .requiresPermission(Permission.RACING_ADMIN.toString());

    carbon
      .addCommand("racing help")
      .withHandler(new CommandHelp());
  }

  @Override
  public void onEnable() {
    instance = this;

    isNoteBlockAPILoaded = Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI");
    isHolographicDisplaysLoaded = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
    isVaultLoaded = Bukkit.getPluginManager().isPluginEnabled("Vault");

    if (isVaultLoaded) {
      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
        economy = rsp.getProvider();
      }
    }

    carbon = new Carbon();

    if(!RaceConfiguration.init(this)) {
      getLogger().log(Level.SEVERE, "*** This plugin will be disabled. ***");
      setEnabled(false);
      return;
    }

    translations = new Translations(this);
    if(!translations.selectLanguage(RaceConfiguration.getValue(ConfigKey.LANGUAGE))) {
      getLogger().log(Level.SEVERE, "*** This plugin will be disabled. ***");
      setEnabled(false);
      return;
    }

    MessageManager.setLanguageTranslation(translations.getSelectedLanguage());

    if(isNoteBlockAPILoaded) {
      SongManager.init(this);
    }

    racingManager = new RacingManager();
    getServer().getPluginManager().registerEvents(racingManager, this);

    StorageType storageType = RaceConfiguration.getValue(ConfigKey.STORAGE);
    switch (storageType) {
      case FILE:
        racingManager.setAPI(new FileAPI(this));
        break;
      case CUSTOM:
        break;
      default:
    }

    setupCommands();
    racingManager.load();
    initMcMMO();
  }

  @Override
  public void onDisable() {
    if(racingManager != null) {
      racingManager.shutdown();
    }
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return carbon.handleCommand(sender, command, args);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return carbon.handleAutoComplete(sender, command, args);
  }

  void initMcMMO() {
    Plugin plugin = getServer().getPluginManager().getPlugin("mcMMO");

    if (!(plugin instanceof mcMMO)) {
      return;
    }

    Bukkit.getPluginManager().registerEvents(new McMMOListener(racingManager), Racing.getInstance());
  }
}
