package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class RailNetConfig {

    private final boolean undergroundRailways;

    private RailNetConfig(boolean undergroundRailways) {
        this.undergroundRailways = undergroundRailways;
    }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public boolean hasUndergroundRailways() {
        return undergroundRailways;
    }

    public static RailNetConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        boolean enabled = ConfigReaderSimple.getBoolean(config, customLogger, "underground-railways", "'underground-railways' flag", true);

        return new RailNetConfig(enabled);
    }

    public String toString() {
        return String.format("underground-railways: %b", undergroundRailways);
    }

}