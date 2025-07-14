package com.gmail.uprial.secretrooms.populator;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import org.bukkit.Chunk;

public abstract class AbstractSeedSpecificPopulator implements ChunkPopulator, Tested_On_1_21_5 {
    final int probability;

    public AbstractSeedSpecificPopulator(final int probability) {
        this.probability = probability;
    }

    protected abstract void populateAppropriateChunk(final Chunk chunk);

    @Override
    public void populate(final Chunk chunk) {
        if (isAppropriate(chunk)) {
            populateAppropriateChunk(chunk);
        }
    }

    // For testing purposes
    static boolean isAppropriate(final BlockSeed bs, final int probability) {
        return bs.oneOf(probability) == 0;
    }

    boolean isAppropriate(final Chunk chunk) {
        return isAppropriate(BlockSeed.valueOf(chunk), probability);
    }
}
