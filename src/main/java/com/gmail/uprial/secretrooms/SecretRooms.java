package com.gmail.uprial.secretrooms;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.config.InvalidConfigException;
import com.gmail.uprial.secretrooms.fixtures.*;
import com.gmail.uprial.secretrooms.listeners.*;
import com.gmail.uprial.secretrooms.populator.ChunkPopulator;
import com.gmail.uprial.secretrooms.populator.Populator;
import com.gmail.uprial.secretrooms.populator.dungeon.DungeonPopulator;
import com.gmail.uprial.secretrooms.populator.endmansion.EndMansionPopulator;
import com.gmail.uprial.secretrooms.populator.loot.LootPopulator;
import com.gmail.uprial.secretrooms.populator.whirlpool.WhirlpoolPopulator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static com.gmail.uprial.secretrooms.SecretRoomsCommandExecutor.COMMAND_NS;

public final class SecretRooms extends JavaPlugin {
    private final String CONFIG_FILE_NAME = "config.yml";
    private final File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);

    private CustomLogger consoleLogger = null;

    private Populator populator = null;

    private TurretCron turretCron = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        consoleLogger = new CustomLogger(getLogger());
        final SecretRoomsConfig secretRoomsConfig = loadConfig(getConfig(), consoleLogger);

        for(final String worldName : Arrays.asList(
                secretRoomsConfig.getNetherName(),
                secretRoomsConfig.getEndName())) {
            if(getServer().getWorld(worldName) == null) {
                throw new RuntimeException(String.format("World '%s' not found", worldName));
            }
        }

        LootPopulator.NETHER_NAME = secretRoomsConfig.getNetherName();
        LootPopulator.END_NAME = secretRoomsConfig.getEndName();

        turretCron = new TurretCron(this, consoleLogger, secretRoomsConfig.getTurretAimingTimeoutInMs());

        final List<ChunkPopulator> chunkPopulators = new ArrayList<>();

        // Order does matter: RailWay is a top priority.
        chunkPopulators.add(new EndMansionPopulator(consoleLogger, secretRoomsConfig.getEndName()));

        chunkPopulators.add(new WhirlpoolPopulator(consoleLogger));
        chunkPopulators.add(new DungeonPopulator(consoleLogger, secretRoomsConfig.getDistanceDensity()));
        // Order does matter: populate chests in RailWay and Whirlpool.
        chunkPopulators.add(new LootPopulator(this, consoleLogger, secretRoomsConfig.getDistanceDensity()));

        populator = new Populator(this, consoleLogger, chunkPopulators);

        getServer().getPluginManager().registerEvents(new TakeAimAdapter(this), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(populator), this);
        getServer().getPluginManager().registerEvents(new TurretListener(), this);
        //getServer().getPluginManager().registerEvents(new InventoryCleanupListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new EntityCleanupListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new SpawnerCleanupListener(consoleLogger), this);
        getServer().getPluginManager().registerEvents(new RaidDebugListener(this, consoleLogger), this);

        getCommand(COMMAND_NS).setExecutor(new SecretRoomsCommandExecutor(this));
        consoleLogger.info("Plugin enabled");
    }

    int repopulateLoaded(final String worldName, final int x, final int z, final int radius) {
        return populator.repopulateLoaded(worldName, x, z, radius);
    }

    void populatePlayer(final Player player, final int density) {
        new LootPopulator(this, consoleLogger, null).populatePlayer(player, density);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        populator.stop();
        turretCron.cancel();
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

    static SecretRoomsConfig loadConfig(FileConfiguration config, CustomLogger customLogger) {
        return loadConfig(config, customLogger, null);
    }

    private static SecretRoomsConfig loadConfig(FileConfiguration config, CustomLogger mainLogger, CustomLogger secondLogger) {
        SecretRoomsConfig secretRoomsConfig = null;

        try {
            final boolean isDebugMode = SecretRoomsConfig.isDebugMode(config, mainLogger);
            mainLogger.setDebugMode(isDebugMode);
            if(secondLogger != null) {
                secondLogger.setDebugMode(isDebugMode);
            }

            secretRoomsConfig = SecretRoomsConfig.getFromConfig(config, mainLogger);
        } catch (InvalidConfigException e) {
            mainLogger.error(e.getMessage());
        }

        return secretRoomsConfig;
    }
}
