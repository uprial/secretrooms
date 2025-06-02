package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderNumbers;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class RailNetConfig {
    private final int distanceDensityMultiplier;

    private RailNetConfig(int distanceDensityMultiplier) {
        this.distanceDensityMultiplier = distanceDensityMultiplier;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public int getDistanceDensityMultiplier() {
        return distanceDensityMultiplier;
    }

    public static RailNetConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        int distanceDensityMultiplier = ConfigReaderNumbers.getInt(config, customLogger, "distance-density-multiplier", "'distance-density-multiplier' value", 0, 100_000);

        return new RailNetConfig(distanceDensityMultiplier);
    }

    public String toString() {
        return String.format("distance-density-multiplier: %,d",
                distanceDensityMultiplier);
    }

}