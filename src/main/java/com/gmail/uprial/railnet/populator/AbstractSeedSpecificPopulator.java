package com.gmail.uprial.railnet.populator;

import com.google.common.hash.Hashing;
import org.bukkit.Chunk;

public abstract class AbstractSeedSpecificPopulator implements ChunkPopulator {
    final String worldName;
    /*
        ==== DENSITY 100 TEST ====

        version 1.21.3
        seed -1565193744182814265 (Belongings 2025-01-12)
        TerraformGenerator-17.0.1
        WorldBorder 1050 x 1050

        $ grep oceanic- plugins/TerraformGenerator/config.yml
        oceanic-frequency: 0.11
        oceanic-threshold: <oceanic-threshold>
        deep-oceanic-threshold: 27.0

        $ grep "Whirlpool.*] populated" logs/latest.log | wc -l
        <whirlpools>

        oceanic-threshold | whirlpools | barrels | chests
                      8.0 | 121              105 | 630
                     22.0 | 83               102 | 565
     */
    final int density;

    public AbstractSeedSpecificPopulator(final String worldName, final int density) {
        this.worldName = worldName;
        this.density = density;
    }

    protected abstract void populateAppropriateChunk(final Chunk chunk);

    @Override
    public void populate(final Chunk chunk) {
        if (isAppropriate(chunk)) {
            populateAppropriateChunk(chunk);
        }
    }

    static long getHash(final long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    static boolean isAppropriate(final int x, final int z, final long seed, final long density) {
        return (getHash(seed * x * z) % density) == 0;
    }

    private boolean isAppropriate(final Chunk chunk) {
        return (chunk.getWorld().getName().equalsIgnoreCase(worldName))
                && isAppropriate(chunk.getX(), chunk.getZ(), chunk.getWorld().getSeed(), density);
    }
}
