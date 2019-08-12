package com.github.hornta.race;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.hornta.*;
import com.github.hornta.race.api.FileAPI;
import com.github.hornta.race.api.StorageType;
import com.github.hornta.race.commands.*;
import com.github.hornta.race.commands.argumentHandlers.*;
import com.github.hornta.race.config.ConfigKey;
import com.github.hornta.race.config.RaceConfiguration;
import com.github.hornta.race.enums.Permission;
import com.github.hornta.race.mcmmo.McMMOListener;
import com.github.hornta.race.message.Translation;
import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.message.Translations;
import com.gmail.nossr50.mcMMO;
import net.dv8tion.jda.core.JDA;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
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
  private Metrics metrics;

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

  public Translations getTranslations() {
    return translations;
  }

  public RacingManager getRacingManager() {
    return racingManager;
  }

  private void setupCommands() {
    carbon = new Carbon();

    carbon.setNoPermissionHandler((CommandSender commandSender, CarbonCommand command) -> {
      MessageManager.sendMessage(commandSender, MessageKey.NO_PERMISSION_COMMAND);
    });

    carbon.setMissingArgumentHandler((CommandSender commandSender, CarbonCommand command) -> {
      MessageManager.setValue("usage", command.getHelpText());
      MessageManager.sendMessage(commandSender, MessageKey.MISSING_ARGUMENTS_COMMAND);
    });

    carbon.setMissingCommandHandler((CommandSender sender, List<CarbonCommand> suggestions) -> {
      MessageManager.setValue("suggestions", suggestions.stream()
        .map(CarbonCommand::getHelpText)
        .collect(Collectors.joining("\n")));
      MessageManager.sendMessage(sender, MessageKey.COMMAND_NOT_FOUND);
    });

    carbon.handleValidation((ValidationResult result) -> {
      switch (result.getStatus()) {
        case ERR_INCORRECT_TYPE:
          MessageManager.setValue("help_text", result.getCommand().getHelpText());
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
          MessageManager.setValue("help_text", result.getCommand().getHelpText());
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

        case ERR_OTHER:
          if(result.getArgument().getType() == CarbonArgumentType.POTION_EFFECT) {
            MessageManager.setValue("potion_effect", result.getValue());
            MessageManager.sendMessage(result.getCommandSender(), MessageKey.POTION_EFFECT_NOT_FOUND);
          }
          break;
      }
    });

    CarbonArgument raceArgument =
      new CarbonArgument.Builder("race")
      .setHandler(new RaceArgumentHandler(racingManager, true))
      .create();

    carbon
      .addCommand("racing create")
      .withHandler(new CommandCreateRace(racingManager))
      .withArgument(
        new CarbonArgument.Builder("race")
          .setHandler(new RaceArgumentHandler(racingManager, false))
          .showTabCompletion(false)
          .create()
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

    CarbonArgument checkpointArgument = new CarbonArgument.Builder("point")
      .setHandler(new CheckpointArgumentHandler(racingManager, true))
      .dependsOn(raceArgument)
      .create();

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
        new CarbonArgument.Builder("state")
          .setHandler(new RaceStateArgumentHandler())
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing setname")
      .withHandler(new CommandSetRaceName(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("name")
          .setType(CarbonArgumentType.STRING)
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing settype")
      .withHandler(new CommandSetType(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("type")
          .setHandler(new RaceTypeArgumentHandler())
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    if(economy != null) {
      carbon
        .addCommand("racing setentryfee")
        .withHandler(new CommandSetEntryFee(racingManager))
        .withArgument(raceArgument)
        .withArgument(
          new CarbonArgument.Builder("fee")
            .setType(CarbonArgumentType.NUMBER)
            .setMin(0)
            .create()
        )
        .requiresPermission(Permission.RACING_MODIFY.toString());
    }

    carbon
      .addCommand("racing setwalkspeed")
      .withHandler(new CommandSetWalkSpeed(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("speed")
          .setType(CarbonArgumentType.NUMBER)
          .setMin(0)
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing addpotioneffect")
      .withHandler(new CommandAddPotionEffect(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("effect")
          .setType(CarbonArgumentType.POTION_EFFECT)
          .create()
      )
      .withArgument(
        new CarbonArgument.Builder("amplifier")
          .setType(CarbonArgumentType.INTEGER)
          .setMin(0)
          .setMax(255)
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing removepotioneffect")
      .withHandler(new CommandRemovePotionEffect(racingManager))
      .withArgument(raceArgument)
      .withArgument(
        new CarbonArgument.Builder("effect")
          .setHandler(new RacePotionEffectArgumentHandler(racingManager))
          .dependsOn(raceArgument)
          .create()
      )
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing clearpotioneffects")
      .withHandler(new CommandClearPotionEffects(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString());

    carbon
      .addCommand("racing addstartpoint")
      .withHandler(new CommandAddStartpoint(racingManager))
      .withArgument(raceArgument)
      .requiresPermission(Permission.RACING_MODIFY.toString())
      .preventConsoleCommandSender();


    CarbonArgument startPointArgument =
      new CarbonArgument.Builder("point")
        .setHandler(new StartPointArgumentHandler(racingManager, true))
        .dependsOn(raceArgument)
        .create();

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
      CarbonArgument songArgument =
        new CarbonArgument.Builder("song")
          .setHandler(new SongArgumentHandler())
          .create();

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

    CarbonArgument lapsArgument = new CarbonArgument.Builder("laps")
      .setType(CarbonArgumentType.INTEGER)
      .setDefaultValue(1)
      .setMin(1)
      .create();

    carbon
      .addCommand("racing start")
      .withHandler(new CommandStartRace(racingManager))
      .withArgument(raceArgument)
      .withArgument(lapsArgument)
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    carbon
      .addCommand("racing startrandom")
      .withHandler(new CommandStartRace(racingManager))
      .withArgument(lapsArgument)
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
      .addCommand("racing leave")
      .withHandler(new CommandLeave(racingManager))
      .requiresPermission(Permission.RACING_PLAYER.toString())
      .preventConsoleCommandSender();

    carbon
      .addCommand("racing reload")
      .withHandler(new CommandReload())
      .requiresPermission(Permission.RACING_ADMIN.toString());

    carbon
      .addCommand("racing help")
      .withHandler(new CommandHelp())
      .requiresPermission(Permission.RACING_PLAYER.toString());

    carbon
      .addCommand("racing info")
      .withArgument(raceArgument)
      .withHandler(new CommandInfo(racingManager))
      .requiresPermission(Permission.RACING_MODERATOR.toString());

    CarbonArgument statArgument = new CarbonArgument.Builder("stat")
      .setHandler(new RaceStatArgumentHandler())
      .create();

    carbon
      .addCommand("racing top")
      .withArgument(raceArgument)
      .withArgument(statArgument)
      .withHandler(new CommandTop(racingManager))
      .requiresPermission(Permission.RACING_PLAYER.toString());

    carbon
      .addCommand("racing resettop")
      .withArgument(raceArgument)
      .withHandler(new CommandResetTop(racingManager))
      .requiresPermission(Permission.RACING_ADMIN.toString());
  }

  @Override
  public void onEnable() {
    instance = this;
    metrics = new Metrics(this);
    isNoteBlockAPILoaded = Bukkit.getPluginManager().isPluginEnabled("NoteBlockAPI");
    isHolographicDisplaysLoaded = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
    isVaultLoaded = Bukkit.getPluginManager().isPluginEnabled("Vault");

    if (isVaultLoaded) {
      RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
      if (rsp != null) {
        economy = rsp.getProvider();
      }
    }

    if (!RaceConfiguration.init(this)) {
      getLogger().log(Level.SEVERE, "*** This plugin will be disabled. ***");
      setEnabled(false);
      return;
    }

    translations = new Translations(this);
    Translation translation = translations.createTranslation(RaceConfiguration.getValue(ConfigKey.LANGUAGE));
    if (translation == null || !translation.load()) {
      getLogger().log(Level.SEVERE, "*** This plugin will be disabled. ***");
      setEnabled(false);
      return;
    }

    MessageManager.setTranslation(translation);

    Translation fallbackTranslation = null;
    if (!translation.getLanguage().equals("english")) {
      fallbackTranslation = translations.createTranslation("english");
      if (fallbackTranslation == null || !fallbackTranslation.load()) {
        getLogger().log(Level.SEVERE, "*** This plugin will be disabled. ***");
        setEnabled(false);
        return;
      }
    }

    MessageManager.setFallbackTranslation(fallbackTranslation);

    if(isNoteBlockAPILoaded) {
      SongManager.init(this);
      getServer().getPluginManager().registerEvents(SongManager.getInstance(), this);
    }

    racingManager = new RacingManager();
    SignManager signManager = new SignManager(racingManager);
    DiscordManager discordManager = new DiscordManager();

    getServer().getPluginManager().registerEvents(racingManager, this);
    getServer().getPluginManager().registerEvents(signManager, this);
    getServer().getPluginManager().registerEvents(discordManager, this);

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
