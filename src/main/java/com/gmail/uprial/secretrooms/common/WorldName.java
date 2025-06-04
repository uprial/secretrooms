package com.gmail.uprial.secretrooms.common;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class WorldName {
    public static final String WORLD = "world";
    public static final String NETHER = "world_nether";
    public static final String END = "world_the_end";

    private static final Set<String> all = ImmutableSet.<String>builder()
            .add(WORLD)
            .add(NETHER)
            .add(END)
            .build();

    public static Set<String> getAll() {
        return all;
    }
}
