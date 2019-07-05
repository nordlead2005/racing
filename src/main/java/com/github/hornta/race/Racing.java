package com.github.hornta.race;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.hornta.Carbon;
import com.github.hornta.CarbonCommand;
import com.github.hornta.race.api.FileAPI;
import com.github.hornta.race.api.StorageType;
import com.github.hornta.race.commands.CommandAddCheckpoint;
import com.github.hornta.race.commands.CommandAddStartpoint;
import com.github.hornta.race.commands.CommandCreateRace;
import com.github.hornta.race.commands.CommandDeleteCheckpoint;
import com.github.hornta.race.commands.CommandDeleteRace;
import com.github.hornta.race.commands.CommandDeleteStartpoint;
import com.github.hornta.race.commands.CommandDisableRace;
import com.github.hornta.race.commands.CommandEnableRace;
import com.github.hornta.race.commands.CommandJoinRace;
import com.github.hornta.race.commands.CommandPlaySong;
import com.github.hornta.race.commands.CommandRaceSetSpawn;
import com.github.hornta.race.commands.CommandRaceSpawn;
import com.github.hornta.race.commands.CommandRaceTeleportPoint;
import com.github.hornta.race.commands.CommandRaceTeleportStart;
import com.github.hornta.race.commands.CommandRaces;
import com.github.hornta.race.commands.CommandSetRaceName;
import com.github.hornta.race.commands.CommandSetSong;
import com.github.hornta.race.commands.CommandSetType;
import com.github.hornta.race.commands.CommandSkipWait;
import com.github.hornta.race.commands.CommandStartEditRace;
import com.github.hornta.race.commands.CommandStartRace;
import com.github.hornta.race.commands.CommandStopEditRace;
import com.github.hornta.race.commands.CommandStopRace;
import com.github.hornta.race.commands.CommandStopSong;
import com.github.hornta.race.commands.CommandUnsetSong;
import com.github.hornta.race.commands.completers.PointCompleter;
import com.github.hornta.race.commands.completers.RaceCompleter;
import com.github.hornta.race.commands.completers.RacingTypeCompleter;
import com.github.hornta.race.commands.completers.SongCompleter;
import com.github.hornta.race.commands.completers.StartPointCompleter;
import com.github.hornta.race.commands.validators.PointExistValidator;
import com.github.hornta.race.commands.validators.RaceExistValidator;
import com.github.hornta.race.commands.validators.RacingTypeValidator;
import com.github.hornta.race.commands.validators.SongExistValidator;
import com.github.hornta.race.commands.validators.StartPointExistValidator;
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
      .addHelpText("/racing create <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldNotExist)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing delete")
      .withHandler(new CommandDeleteRace(racingManager))
      .addHelpText("/racing delete <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing list")
      .withHandler(new CommandRaces(racingManager))
      .addHelpText("/racing list")
      .requiresPermission(Permission.RACING_PLAYER.toString());

    carbon
      .addCommand("racing addcheckpoint")
      .withHandler(new CommandAddCheckpoint(racingManager))
      .addHelpText("/racing addcheckpoint <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing deletecheckpoint")
      .withHandler(new CommandDeleteCheckpoint(racingManager))
      .addHelpText("/racing deletecheckpoint <race> <point>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, checkpointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, checkpointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpcheckpoint")
      .withHandler(new CommandRaceTeleportPoint(racingManager))
      .addHelpText("/racing tpcheckpoint <race> <point>")
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
      .addHelpText("/racing spawn <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing setspawn")
      .withHandler(new CommandRaceSetSpawn(racingManager))
      .addHelpText("/racing setspawn <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing disable")
      .withHandler(new CommandDisableRace(racingManager))
      .addHelpText("/racing disable <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing enable")
      .withHandler(new CommandEnableRace(racingManager))
      .addHelpText("/racing enable <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing setname")
      .withHandler(new CommandSetRaceName(racingManager))
      .addHelpText("/racing setname <race> <name>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing startedit")
      .withHandler(new CommandStartEditRace(racingManager))
      .addHelpText("/racing startedit <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing stopedit")
      .withHandler(new CommandStopEditRace(racingManager))
      .addHelpText("/racing stopedit <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing addstartpoint")
      .withHandler(new CommandAddStartpoint(racingManager))
      .addHelpText("/racing addstartpoint <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing deletestartpoint")
      .withHandler(new CommandDeleteStartpoint(racingManager))
      .addHelpText("/racing deletestartpoint <race> <position>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, startPointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, startPointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing tpstartpoint")
      .withHandler(new CommandRaceTeleportStart(racingManager))
      .addHelpText("/racing tpstartpoint <race> <position>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(new int[] { 0, 1 }, startPointShouldExist)
      .setTabComplete(new int[] { 0, 1 }, startPointCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing settype")
      .withHandler(new CommandSetType(racingManager))
      .addHelpText("/racing settype <race> <type>")
      .setNumberOfArguments(2)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .validateArgument(1, new RacingTypeValidator())
      .setTabComplete(1, new RacingTypeCompleter())
      .requiresPermission(Permission.RACING_MODIFY.toString());

    if(isNoteBlockAPILoaded) {
      carbon
        .addCommand("racing setsong")
        .withHandler(new CommandSetSong(racingManager))
        .addHelpText("/racing setsong <race> <song>")
        .setNumberOfArguments(2)
        .validateArgument(0, raceShouldExist)
        .setTabComplete(0, raceCompleter)
        .validateArgument(1, songExistValidator)
        .setTabComplete(1, songCompleter)
        .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing unsetsong")
      .withHandler(new CommandUnsetSong(racingManager))
      .addHelpText("/racing unsetsong <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODIFY.toString());
    }

    carbon
      .addCommand("racing start")
      .withHandler(new CommandStartRace(racingManager))
      .addHelpText("/racing start <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing join")
      .withHandler(new CommandJoinRace(racingManager))
      .addHelpText("/racing join <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing stop")
      .withHandler(new CommandStopRace(racingManager))
      .addHelpText("/racing stop <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    carbon
      .addCommand("racing skipwait")
      .withHandler(new CommandSkipWait(racingManager))
      .addHelpText("/racing skipwait <race>")
      .setNumberOfArguments(1)
      .validateArgument(0, raceShouldExist)
      .setTabComplete(0, raceCompleter)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    if(isNoteBlockAPILoaded) {
      carbon
        .addCommand("racing playsong")
        .withHandler(new CommandPlaySong())
        .addHelpText("/racing playsong <song>")
        .setNumberOfArguments(1)
        .validateArgument(0, songExistValidator)
        .setTabComplete(0, songCompleter)
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();

      carbon
        .addCommand("racing stopsong")
        .withHandler(new CommandStopSong())
        .addHelpText("/racing stopsong")
        .requiresPermission(Permission.RACING_MODIFY.toString())
        .preventConsoleCommandSender();
    }

    carbon
      .addCommand("racing reload")
      .withHandler((CommandSender sender, String[] args) -> {
        RaceConfiguration.reload(this);

        if(!racingManager.getRaceSessions().isEmpty()) {
          MessageManager.sendMessage(sender, MessageKey.RELOAD_NOT_RACES);
        } else {
          racingManager.load();
        }

        MessageManager.sendMessage(sender, MessageKey.RELOAD_SUCCESS);
      })
      .addHelpText("/racing reload")
      .requiresPermission(Permission.RACING_ADMIN.toString());
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
