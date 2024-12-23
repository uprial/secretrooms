package com.gmail.uprial.railnet.firework;

import org.bukkit.Color;

class FireworkMagicColor {
    /*
        Avoid any changes on the already generated map:
        the already generated and crafted fireworks will stop working.
     */
    private static final int MAGIC_RED = Color.RED.getRed() - 1;

    static Color encode(final FireworkMagic magic) {
        return Color.fromRGB(MAGIC_RED, magic.getType(), magic.getAmount());
    }

    static FireworkMagic decode(final Color color) {
        if((color.getRed() == MAGIC_RED)) {
            return new FireworkMagic(color.getGreen(), color.getBlue());
        } else {
            return null;
        }
    }
}
