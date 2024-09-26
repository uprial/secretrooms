package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.config.ConfigReaderMaterial;
import com.gmail.uprial.railnet.config.ConfigReaderSimple;
import com.gmail.uprial.railnet.config.InvalidConfigException;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import static com.gmail.uprial.railnet.common.DoubleHelper.MIN_DOUBLE_VALUE;
import static com.gmail.uprial.railnet.config.ConfigReaderNumbers.getDouble;

public final class RailNetConfig {
    private final double minecartMaxSpeed;
    private final double minecartSlowSpeed;
    private final Material minecartSlowBlock;

    private RailNetConfig(
            final double minecartMaxSpeed,
            final double minecartSlowSpeed,
            final Material minecartSlowBlock
    ) {
        this.minecartMaxSpeed = minecartMaxSpeed;
        this.minecartSlowSpeed = minecartSlowSpeed;
        this.minecartSlowBlock = minecartSlowBlock;
    }

    public double getMinecartMaxSpeed() {
        return minecartMaxSpeed;
    }

    public double getMinecartSlowSpeed() { return minecartSlowSpeed; }

    public Material getMinecartSlowBlock() { return minecartSlowBlock; }

    static boolean isDebugMode(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        return ConfigReaderSimple.getBoolean(config, customLogger, "debug", "'debug' flag", false);
    }

    public static RailNetConfig getFromConfig(FileConfiguration config, CustomLogger customLogger) throws InvalidConfigException {
        final double minecartMaxSpeed = getDouble(config, customLogger, "minecart-max-speed", "minecart max speed", MIN_DOUBLE_VALUE, 1000);
        final double minecartSlowSpeed = getDouble(config, customLogger, "minecart-slow-speed", "minecart slow speed", MIN_DOUBLE_VALUE, 1000);
        final Material minecartSlowBlock = ConfigReaderMaterial.getMaterial(config, "minecart-slow-block", "minecart slow block");

        return new RailNetConfig(
                minecartMaxSpeed,
                minecartSlowSpeed,
                minecartSlowBlock
        );
    }

    public String toString() {
        return String.format("minecart-max-speed: %.2f, " +
                "minecart-slow-speed: %.2f, " +
                "minecart-slow-block: %s",
                minecartMaxSpeed,
                minecartSlowSpeed,
                minecartSlowBlock);
    }
}
