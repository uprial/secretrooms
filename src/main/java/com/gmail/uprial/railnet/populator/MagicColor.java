package com.gmail.uprial.railnet.populator;

import org.bukkit.Color;

public class MagicColor {
    private static final int BYTE_CAPACITY = Color.RED.getRed() + 1;

    /*
        Avoid any changes on the already generated map:
        the already generated and crafted fireworks will stop working.
     */
    private static final int MAGIC_RED = Color.RED.getRed() - 1;

    public static Color encode(final int power) {
        return Color.fromRGB(MAGIC_RED, power / BYTE_CAPACITY, power % BYTE_CAPACITY);
    }

    public static Integer decode(final Color color) {
        if((color.getRed() == MAGIC_RED)) {
            return color.getBlue() + color.getGreen() * BYTE_CAPACITY;
        } else {
            return null;
        }
    }
}
