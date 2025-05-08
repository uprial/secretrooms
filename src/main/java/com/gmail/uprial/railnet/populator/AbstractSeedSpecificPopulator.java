package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.HashUtils;
import com.gmail.uprial.railnet.common.WorldName;
import org.bukkit.Chunk;

public abstract class AbstractSeedSpecificPopulator implements ChunkPopulator, Tested_On_1_21_5 {
    final String worldName;
    final int density;

    static class InternalPopulatorConfigurationError extends RuntimeException {
        InternalPopulatorConfigurationError(String message) {
            super(message);
        }
    }

    public AbstractSeedSpecificPopulator(final String worldName, final int density) {
        if((worldName != null) && (!WorldName.getAll().contains(worldName))) {
            throw new InternalPopulatorConfigurationError(String.format("Unknown world: %s", worldName));
        }
        this.worldName = worldName;

        this.density = density;
    }

    protected abstract boolean populateAppropriateChunk(final Chunk chunk, final PopulationHistory history);

    @Override
    public boolean populate(final Chunk chunk, final PopulationHistory history) {
        if (isAppropriate(chunk)) {
            return populateAppropriateChunk(chunk, history);
        } else {
            return false;
        }
    }

    static boolean isAppropriate(final long x, final long z, final long seed, final long density) {
        /*
            The method must be
            - consistent
            - evenly distributed
            - asymmetric
         */
        return (HashUtils.getHash(seed * x * z
                + (seed % density) * x
                + (seed / density + density / seed) * z) % density) == 0;
    }

    private boolean isAppropriate(final Chunk chunk) {
        return (worldName == null || chunk.getWorld().getName().equals(worldName))
                && isAppropriate(chunk.getX(), chunk.getZ(), chunk.getWorld().getSeed(), density);
    }
}
