package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.HashUtils;
import com.gmail.uprial.railnet.common.WorldName;
import org.bukkit.Chunk;

public abstract class AbstractSeedSpecificPopulator implements ChunkPopulator {
    final String worldName;
    /*
        Test

            version 1.21.3
            seed -1565193744182814265 (Belongings 2025-01-12)
            TerraformGenerator-19.1.0
            WorldBorder 4050 x 4050

            $ grep oceanic- plugins/TerraformGenerator/config.yml
            oceanic-frequency: 0.11
            oceanic-threshold: 8.0
            deep-oceanic-threshold: 27.0

            $ grep "Whirlpool.*] populated" logs/latest.log | wc -l
            14

            My guess was that the prime numbers are more stable, but they are not.

            density-1 | density-2 | Whirlpool | Dungeon
                  100 | 300                34 | 21
                   99 | 333                76 | 34
                  101 | 307                50 | 12
                   97 | 293                58 | 3

                  200 | 500                14 | 8
     */
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

    static boolean isAppropriate(final int x, final int z, final long seed, final long density) {
        return (HashUtils.getHash(seed * x * z) % density) == 0;
    }

    private boolean isAppropriate(final Chunk chunk) {
        return (worldName == null || chunk.getWorld().getName().equalsIgnoreCase(worldName))
                && isAppropriate(chunk.getX(), chunk.getZ(), chunk.getWorld().getSeed(), density);
    }
}
