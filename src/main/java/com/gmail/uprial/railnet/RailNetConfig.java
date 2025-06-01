package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderNumbers;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class RailNetConfig {

    private final boolean undergroundRailways;
    private final int distanceDensityMultiplier;

    private RailNetConfig(boolean undergroundRailways,
                          int distanceDensityMultiplier) {
        this.undergroundRailways = undergroundRailways;
        this.distanceDensityMultiplier = distanceDensityMultiplier;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public boolean hasUndergroundRailways() {
        return undergroundRailways;
    }

    public int getDistanceDensityMultiplier() {
        return distanceDensityMultiplier;
    }

    public static RailNetConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        boolean undergroundRailways = ConfigReaderSimple.getBoolean(config, customLogger, "underground-railways", "'underground-railways' flag");
        int distanceDensityMultiplier = ConfigReaderNumbers.getInt(config, customLogger, "distance-density-multiplier", "'distance-density-multiplier' value", 0, 100_000);

        return new RailNetConfig(undergroundRailways, distanceDensityMultiplier);
    }

    public String toString() {
        return String.format("underground-railways: %b, distance-density-multiplier: %,d",
                undergroundRailways, distanceDensityMultiplier);
    }

}