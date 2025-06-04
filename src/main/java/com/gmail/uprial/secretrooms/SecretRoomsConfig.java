package com.gmail.uprial.secretrooms;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.config.ConfigReaderNumbers;
import com.gmail.uprial.secretrooms.config.ConfigReaderSimple;
import com.gmail.uprial.secretrooms.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class SecretRoomsConfig {
    private final int distanceDensityMultiplier;

    private SecretRoomsConfig(int distanceDensityMultiplier) {
        this.distanceDensityMultiplier = distanceDensityMultiplier;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public int getDistanceDensityMultiplier() {
        return distanceDensityMultiplier;
    }

    public static SecretRoomsConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        int distanceDensityMultiplier = ConfigReaderNumbers.getInt(config, customLogger, "distance-density-multiplier", "'distance-density-multiplier' value", 0, 100_000);

        return new SecretRoomsConfig(distanceDensityMultiplier);
    }

    public String toString() {
        return String.format("distance-density-multiplier: %,d",
                distanceDensityMultiplier);
    }

}