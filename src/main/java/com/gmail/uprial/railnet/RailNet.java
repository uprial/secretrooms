package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.firework.FireworkEngine;
import com.gmail.uprial.railnet.listeners.*;
import com.gmail.uprial.railnet.populator.Populator;
import com.gmail.uprial.railnet.populator.mineshaft.MineshaftPopulator;
import com.gmail.uprial.railnet.populator.railway.RailWayPopulator;
import com.gmail.uprial.railnet.populator.whirlpool.WhirlpoolPopulator;
import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static com.gmail.uprial.railnet.RailNetCommandExecutor.COMMAND_NS;

public final class RailNet extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;

    private Populator populator = null;

    private FireworkEngine fireworkEngine = null;

    private RailNetCron cron;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        cron = new RailNetCron(this);

        consoleLogger = new CustomLogger(getLogger());
        loadConfig(getConfig(), consoleLogger);

        populator = new Populator(this, consoleLogger,
                Lists.newArrayList(
                        // Order does matter: RailWay is top priority
                        new RailWayPopulator(this, consoleLogger),
                        new WhirlpoolPopulator(this, consoleLogger),
                        // Order does matter: populate chests in RailWay and Whirlpool.
                        new MineshaftPopulator(this, consoleLogger)
                )
        );

        getServer().getPluginManager().registerEvents(new ChunkListener(populator), this);
        getServer().getPluginManager().registerEvents(new NastyEndermanListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new NastyShooterListener(this, consoleLogger), this);
        getServer().getPluginManager().registerEvents(new ExplosiveShooterListener(this, consoleLogger), this);
        getServer().getPluginManager().registerEvents(new StrongBlockListener(), this);

        fireworkEngine = new FireworkEngine(this, consoleLogger);
        fireworkEngine.enableCraftBook();

        getServer().getPluginManager().registerEvents(new ExplosiveFireworkListener(fireworkEngine), this);

        getCommand(COMMAND_NS).setExecutor(new RailNetCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    void reloadConfig(CustomLogger userLogger) {
        reloadConfig();
        loadConfig(getConfig(), userLogger, consoleLogger);
    }

    int repopulateLoaded(final String worldName, final int x, final int z, final int radius) {
        return populator.repopulateLoaded(worldName, x, z, radius);
    }

    void populatePlayer(final Player player, final int density) {
        new MineshaftPopulator(this, consoleLogger).populatePlayer(player, density);
    }

    @Override
    public void onDisable() {
        fireworkEngine.disableCraftBook();
        HandlerList.unregisterAll(this);
        cron.cancel();
        consoleLogger.info("Plugin disabled");
    }

    @Override
    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            saveResource(CONFIG_FILE_NAME, false);
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return YamlConfiguration.loadConfiguration(configFile);
    }

    static void loadConfig(FileConfiguration config, CustomLogger customLogger) {
        loadConfig(config, customLogger, null);
    }

    private static void loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        try {
            final boolean isDebugMode = RailNetConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }
    }
}
