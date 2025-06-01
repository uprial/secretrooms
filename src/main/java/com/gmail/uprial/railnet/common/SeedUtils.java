package com.gmail.uprial.railnet.common;

import com.google.common.hash.Hashing;

public class SeedUtils {
    public static long getHash(final long seed, final long x, final long z) {
        /*
            The method must be
            1 consistent
            2 evenly distributed
            3 asymmetric

            For 1 and 2, we need a consistent and evenly distributed hash function.

            For 3, we need different multipliers for x and z.
         */
        long semiSeed = (long) Math.sqrt(seed);
        return getHash(seed
                // Not sure why
                * x * z
                + (seed / semiSeed) * x
                + (seed % semiSeed) * z);
    }

    private static long getHash(final long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }
}
