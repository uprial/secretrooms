package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;

public final class RailNetConfig {

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }
}
