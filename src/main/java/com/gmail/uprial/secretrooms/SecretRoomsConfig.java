package com.gmail.uprial.secretrooms;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.config.ConfigReaderNumbers;
import com.gmail.uprial.secretrooms.config.ConfigReaderSimple;
import com.gmail.uprial.secretrooms.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class SecretRoomsConfig {
    private final int distanceDensityMultiplier;
    private final String netherName;
    private final String endName;

    private SecretRoomsConfig(int distanceDensityMultiplier,
                              final String netherName,
                              final String endName) {
        this.distanceDensityMultiplier = distanceDensityMultiplier;
        this.netherName = netherName;
        this.endName = endName;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public int getDistanceDensityMultiplier() {
        return distanceDensityMultiplier;
    }

    public String getNetherName() {
        return netherName;
    }

    public String getEndName() {
        return endName;
    }

    public static SecretRoomsConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        int distanceDensityMultiplier = ConfigReaderNumbers.getInt(config, customLogger, "distance-density-multiplier", "'distance-density-multiplier' value", 0, 100_000);
        String netherName = ConfigReaderSimple.getString(config, "nether-name", "'nether-name' value");
        String endName = ConfigReaderSimple.getString(config, "end-name", "'end-name' value");

        return new SecretRoomsConfig(distanceDensityMultiplier, netherName, endName);
    }

    public String toString() {
        return String.format("distance-density-multiplier: %,d, nether-name: '%s', end-name: '%s'",
                distanceDensityMultiplier, netherName, endName);
    }

}