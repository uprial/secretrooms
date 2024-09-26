package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.listeners.VehicleListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import static com.gmail.uprial.railnet.RailNetCommandExecutor.COMMAND_NS;

public final class RailNet extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;
    private RailNetConfig railNetConfig = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        railNetConfig = loadConfig(getConfig(), consoleLogger);

        getServer().getPluginManager().registerEvents(new VehicleListener(this, consoleLogger), this);

        getCommand(COMMAND_NS).setExecutor(new RailNetCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    public RailNetConfig getRailNetConfig() {
        return railNetConfig;
    }

    void reloadConfig(CustomLogger userLogger) {
        reloadConfig();
        railNetConfig = loadConfig(getConfig(), userLogger, consoleLogger);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
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

    static RailNetConfig loadConfig(FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    private static RailNetConfig loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        RailNetConfig railNetConfig = null;
        try {
            final boolean isDebugMode = RailNetConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            railNetConfig = RailNetConfig.getFromConfig(config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }

        return railNetConfig;
    }
}
