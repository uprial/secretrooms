package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.BlockSeed;
import com.gmail.uprial.railnet.common.WorldName;
import org.bukkit.Chunk;

public abstract class AbstractSeedSpecificPopulator implements ChunkPopulator, Tested_On_1_21_5 {
    final String worldName;
    final int probability;

    static class InternalPopulatorConfigurationError extends RuntimeException {
        InternalPopulatorConfigurationError(String message) {
            super(message);
        }
    }

    public AbstractSeedSpecificPopulator(final String worldName, final int probability) {
        if((worldName != null) && (!WorldName.getAll().contains(worldName))) {
            throw new InternalPopulatorConfigurationError(String.format("Unknown world: %s", worldName));
        }
        this.worldName = worldName;

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
        return (worldName == null || chunk.getWorld().getName().equals(worldName))
                && (isAppropriate(BlockSeed.valueOf(chunk), probability));
    }
}
