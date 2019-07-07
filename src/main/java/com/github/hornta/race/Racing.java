package com.github.hornta.race;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.hornta.Carbon;
import com.github.hornta.CarbonCommand;
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
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Racing extends JavaPlugin {
  private static Racing instance;
  private boolean isNoteBlockAPILoaded;
  private boolean isHolographicDisplaysLoaded;
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
    RaceExistValidator raceShouldExist = new RaceExistValidator(racingManager, true);
    RaceExistValidator raceShouldNotExist = new RaceExistValidator(racingManager, false);
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

    carbon
      .addCommand("racing create")
      .withHandler(new CommandCreateRace(racingManager))
      .addHelpText("/rc create <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldNotExist)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing delete")
      .withHandler(new CommandDeleteRace(racingManager))
      .addHelpText("/rc delete <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing list")
      .withHandler(new CommandRaces(racingManager))
      .addHelpText("/rc list")
      .requiresPermission(Permission.RACING_PLAYER.toString());

    carbon
      .addCommand("racing addcheckpoint")
      .withHandler(new CommandAddCheckpoint(racingManager))
      .addHelpText("/rc addcheckpoint <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing deletecheckpoint")
      .withHandler(new CommandDeleteCheckpoint(racingManager))
      .addHelpText("/rc deletecheckpoint <race> <point>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, checkpointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, checkpointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpcheckpoint")
      .withHandler(new CommandRaceTeleportPoint(racingManager))
      .addHelpText("/rc tpcheckpoint <race> <point>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, checkpointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, checkpointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing spawn")
      .withHandler(new CommandRaceSpawn(racingManager))
      .addHelpText("/rc spawn <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing setspawn")
      .withHandler(new CommandRaceSetSpawn(racingManager))
      .addHelpText("/rc setspawn <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing setstate")
      .withHandler(new CommandSetRaceState(racingManager))
      .addHelpText("/rc setstate <race> <state>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(1, new RaceStateValidator())
      .setTabComplete(1, new RaceStateCompleter())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing setname")
      .withHandler(new CommandSetRaceName(racingManager))
      .addHelpText("/rc setname <race> <name>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing settype")
      .withHandler(new CommandSetType(racingManager))
      .addHelpText("/rc settype <race> <type>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(1, new RaceTypeValidator())
      .setTabComplete(1, new RaceTypeCompleter())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing addstartpoint")
      .withHandler(new CommandAddStartpoint(racingManager))
      .addHelpText("/rc addstartpoint <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing deletestartpoint")
      .withHandler(new CommandDeleteStartpoint(racingManager))
      .addHelpText("/rc deletestartpoint <race> <position>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, startPointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, startPointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpstartpoint")
      .withHandler(new CommandRaceTeleportStart(racingManager))
      .addHelpText("/rc tpstartpoint <race> <position>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, startPointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, startPointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    if(isNoteBlockAPILoaded) {
      carbon
        .addCommand("racing setsong")
        .withHandler(new CommandSetSong(racingManager))
        .addHelpText("/rc setsong <race> <song>")
        .setNumberOfArguments(2)
        .validateArgument(0, raceShouldExist)
        .setTabComplete(0, raceCompleter)
        .validateArgument(1, songExistValidator)
        .setTabComplete(1, songCompleter)
        .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing unsetsong")
      .withHandler(new CommandUnsetSong(racingManager))
      .addHelpText("/rc unsetsong <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());
    }

    carbon
      .addCommand("racing start")
      .withHandler(new CommandStartRace(racingManager))
      .addHelpText("/rc start <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing join")
      .withHandler(new CommandJoinRace(racingManager))
      .addHelpText("/rc join <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing stop")
      .withHandler(new CommandStopRace(racingManager))
      .addHelpText("/rc stop <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    carbon
      .addCommand("racing skipwait")
      .withHandler(new CommandSkipWait(racingManager))
      .addHelpText("/rc skipwait <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    if(isNoteBlockAPILoaded) {
      carbon
        .addCommand("racing playsong")
        .withHandler(new CommandPlaySong())
        .addHelpText("/rc playsong <song>")
        .setNumberOfArguments(1)
        .validateArgument(0, songExistValidator)
        .setTabComplete(0, songCompleter)
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();

      carbon
        .addCommand("racing stopsong")
        .withHandler(new CommandStopSong())
        .addHelpText("/rc stopsong")
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();
    }

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
      .addHelpText("/rc reload")
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
    return carbon.getCommandManager().handleCommand(sender, command, args);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return carbon.getCommandManager().handleAutoComplete(sender, command, args);
  }

  void initMcMMO() {
    Plugin plugin = getServer().getPluginManager().getPlugin("mcMMO");

    if (!(plugin instanceof mcMMO)) {
      return;
    }

    Bukkit.getPluginManager().registerEvents(new McMMOListener(racingManager), Racing.getInstance());
  }
}
