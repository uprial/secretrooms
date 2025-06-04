package com.gmail.uprial.railnet.common;

import com.google.common.hash.Hashing;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class BlockSeed {
    static class BlockSeedError extends RuntimeException {
        BlockSeedError(String message) {
            super(message);
        }
    }

    final long seed;
    final long x;
    final long z;

    protected BlockSeed(final long seed, final long x, final long z) {
        this.seed = seed;
        this.x = x;
        this.z = z;
    }

    // ==== static constructors ====

    public static BlockSeed valueOf(final World world) {
        return new BlockSeed(world.getSeed(), 0, 0);
    }

    public static BlockSeed valueOf(final Block block) {
        // WARNING: two blocks with the identical X and Z will have the same BlockSeed.
        return new BlockSeed(block.getWorld().getSeed(),
                block.getX(),
                block.getZ());
    }

    public static BlockSeed valueOf(final Entity entity) {
        // WARNING: two entities with the identical X and Z will have the same BlockSeed.
        return new BlockSeed(entity.getWorld().getSeed(),
                entity.getLocation().getBlockX(),
                entity.getLocation().getBlockZ());
    }

    public static BlockSeed valueOf(final Chunk chunk) {
        return new BlockSeed(chunk.getWorld().getSeed(), chunk.getX(), chunk.getZ());
    }

    // ==== random-generation alternatives ====

    public <T> T oneOf(final Set<T> set) {
        return oneOf(new ArrayList<>(set));
    }

    public <T> T oneOf(final List<T> list) {
        return list.get((int) oneOf(list.size()));
    }

    private AtomicLong cachedOneOfHash = null;
    public long oneOf(final long range) {
        if(range < 1) {
            throw new BlockSeedError(String.format("%s range is not a natural number: %d", this, range));
        }

        if(cachedOneOfHash == null) {
            cachedOneOfHash = new AtomicLong(getHash(seed, x, z));
        }

        long index = cachedOneOfHash.get() % range;
        if (index < 0) {
            index += range;
        }

        return index;
    }

    public boolean pass(final long callId, final double probability, final int density) {
        if(getRightDigits(probability, 4) > 3) {
            throw new BlockSeedError(String.format("%s probability has too many digits: %f", this, probability));
        } else if (probability > 100.0D) {
            throw new BlockSeedError(String.format("%s probability too big: %f", this, probability));
        }

        long range = 100_000L;
        long index = getHash(seed + callId, x, z) % range;
        if (index < 0) {
            index += range;
        }

        return index < (long)(probability * 1000.D * (1.0D + density));
    }

    // ==== the main method ====

    static long getHash(final long seed, final long x, final long z) {
        try {
            /*
                The method must be
                1 consistent
                2 evenly distributed
                3 asymmetric

                For 1 and 2, we need a consistent and evenly distributed hash function.

                For 3, we need different multipliers for x and z.
             */
            long semiSeed = (long) (Math.signum(seed) * Math.sqrt(Math.abs(seed)));
            return getHash(seed
                    /*
                        For more even distribution,
                        small seeds should be increased,
                        but big seeds must not be increased.
                     */
                    * (Math.abs(seed) > Integer.MAX_VALUE ? 1 : x * z)
                    + (seed / semiSeed) * x
                    + (seed % semiSeed) * z);
        } catch (java.lang.ArithmeticException e) {
            throw new BlockSeedError(String.format("Wrong BlockSeed[%d:%d:%d]: %s", seed, x, z, e.getMessage()));
        }
    }

    // ==== protected methods ====

    static long getHash(final long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    static int getRightDigits(double value, int maxExponent) {
        int digits = 0;
        //noinspection MethodCallInLoopCondition
        while((Math.abs(round(value, digits) - value) > Double.MIN_VALUE) && (digits < maxExponent)) {
            digits += 1;
        }
        return digits;
    }

    // Round a value to a specific digit
    static double round(double value, int digit) {
        double multiplier = Math.pow(10.0, digit);

        return Math.round(value * multiplier) / multiplier;
    }

    @Override
    public String toString() {
        return String.format("BlockSeed[%d:%d:%d]", seed, x, z);
    }
}
