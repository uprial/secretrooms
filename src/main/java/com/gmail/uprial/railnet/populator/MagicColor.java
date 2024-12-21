package com.gmail.uprial.railnet.populator;

import org.bukkit.Color;

public class MagicColor {
    private static final int MAGIC_RED = 254;
    private static final int MAGIC_GREEN = 1;

    public static Color encode(final int power) {
        return Color.fromRGB(MAGIC_RED, MAGIC_GREEN, power);
    }

    public static Integer decode(final Color color) {
        if((color.getRed() == MAGIC_RED) && (color.getGreen() == MAGIC_GREEN)) {
            return color.getBlue();
        } else {
            return null;
        }
    }
}
