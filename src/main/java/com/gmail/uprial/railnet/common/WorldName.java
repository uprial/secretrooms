package com.gmail.uprial.railnet.common;

import java.util.Locale;

public class WorldName {
    public static String WORLD = "world";
    public static String NETHER = "world_nether";
    public static String END = "world_the_end";

    public static String normalize(final String worldName) {
        return worldName.toLowerCase(Locale.ROOT);
    }
}
