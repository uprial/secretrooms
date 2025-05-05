package com.gmail.uprial.railnet.firework;

import org.bukkit.Color;

class FireworkMagicColor {
    /*
        Avoid any changes on the already generated map:
        the already generated and crafted fireworks will stop working.
     */
    private static final int MAGIC_RED = Color.RED.getRed() - 1;

    private static final int BYTE = Color.RED.getRed() + 1;

    private static final int GREEN_SHIFT = (BYTE - 1) / 3;

    static Color encode(final FireworkMagic magic) {
        if(magic.getType() >= GREEN_SHIFT) {
            throw new FireworkError(String.format("Type too big: %d", magic.getType()));
        } else if (magic.getAmount() >= BYTE * (BYTE / GREEN_SHIFT)) {
            throw new FireworkError(String.format("Amount too big: %d", magic.getAmount()));
        }

        final int green = magic.getAmount() / BYTE * GREEN_SHIFT + magic.getType();
        final int blue = magic.getAmount() % BYTE;

        return Color.fromRGB(MAGIC_RED, green, blue);
    }

    static FireworkMagic decode(final Color color) {
        if((color.getRed() == MAGIC_RED)) {
            final int type = color.getGreen() % GREEN_SHIFT;
            final int amount = (color.getGreen() / GREEN_SHIFT) * BYTE + color.getBlue();

            return new FireworkMagic(type, amount);
        } else {
            return null;
        }
    }
}
