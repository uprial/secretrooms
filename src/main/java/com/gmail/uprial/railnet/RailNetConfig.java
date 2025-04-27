package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class RailNetConfig {

    private final boolean undergroundRailways;
    private final boolean dynamicLootDensity;

    private RailNetConfig(boolean undergroundRailways,
                          boolean dynamicLootDensity) {
        this.undergroundRailways = undergroundRailways;
        this.dynamicLootDensity = dynamicLootDensity;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public boolean hasUndergroundRailways() {
        return undergroundRailways;
    }

    public boolean hasDynamicLootDensity() {
        return dynamicLootDensity;
    }

    public static RailNetConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        boolean undergroundRailways = ConfigReaderSimple.getBoolean(config, customLogger, "underground-railways", "'underground-railways' flag", false);
        boolean dynamicLootDensity = ConfigReaderSimple.getBoolean(config, customLogger, "dynamic-loot-density", "'dynamic-loot-density' flag", true);

        return new RailNetConfig(undergroundRailways, dynamicLootDensity);
    }

    public String toString() {
        return String.format("underground-railways: %b, dynamic-loot-density: %b",
                undergroundRailways, dynamicLootDensity);
    }

}