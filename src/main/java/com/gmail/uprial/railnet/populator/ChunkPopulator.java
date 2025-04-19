package com.gmail.uprial.railnet.populator;

import org.bukkit.Chunk;

public interface ChunkPopulator {
    boolean populate(final Chunk chunk, final PopulationHistory history);

    String getName();
}
