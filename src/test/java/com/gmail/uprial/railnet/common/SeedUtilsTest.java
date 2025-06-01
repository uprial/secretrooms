package com.gmail.uprial.railnet.common;

import com.gmail.uprial.railnet.populator.ChunkXZ;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.gmail.uprial.railnet.common.SeedUtils.getHash;
import static org.junit.Assert.*;

public class SeedUtilsTest {
    private final int TEST_MAP_SIZE = 100;

    @Test
    public void testConsistent() {
        forSeed((final Integer seed) -> {
             assertEquals(getTestMap(seed), getTestMap(seed));
        });
    }

    @Test
    public void testEvenlyDistributed() {
        final int numberOfCells = (2 * TEST_MAP_SIZE + 1) * (2 * TEST_MAP_SIZE + 1);

        forSeed((final Integer seed) -> {
            assertEquals(numberOfCells, getTestMap(seed).size());
        });
    }

    @Test
    public void testAsymmetric() {
        forSeed((final Integer seed) -> {

            final Map<ChunkXZ, Long> map = getTestMap(seed);

            for (final Map.Entry<ChunkXZ, Long> entry : map.entrySet()) {
                final ChunkXZ xz = entry.getKey();
                final long hash = entry.getValue();

                assertEquals(String.format("%s is self-symmetric", xz),
                        hash,
                        (long)map.get(new ChunkXZ(+xz.getX(), +xz.getZ())));

                final Set<ChunkXZ> symmetric = new HashSet<>();
                // Simple sign inversion
                for(int x = -1; x <= +1; x+=2) {
                    for (int z = -1; z <= +1; z+=2) {
                        if ((x != +1) && (z != +1)) {
                            final ChunkXZ _xz = new ChunkXZ(x * xz.getX(), z * xz.getZ());
                            if (!xz.equals(_xz) && (hash == map.get(_xz))) {
                                symmetric.add(_xz);
                            }
                        }
                    }
                }
                // Sign and value inversion
                for(int x = -1; x <= +1; x+=2) {
                    for (int z = -1; z <= +1; z+=2) {
                        final ChunkXZ _xz = new ChunkXZ(x * xz.getZ(), z * xz.getX());
                        if (!xz.equals(_xz) && (hash == map.get(_xz))) {
                            symmetric.add(_xz);
                        }
                    }
                }
                assertTrue(String.format("%s symmetry for %d: %s", xz, seed, symmetric),
                        symmetric.size() < 2);
            }
        });
    }

    private Map<ChunkXZ, Long> getTestMap(final long seed) {
        final Map<ChunkXZ, Long> map = new HashMap<>();
        for (int x = -TEST_MAP_SIZE; x <= TEST_MAP_SIZE; x++) {
            for (int z = -TEST_MAP_SIZE; z <= TEST_MAP_SIZE; z++) {
                map.put(new ChunkXZ(x, z), getHash(seed, x, z));
            }
        }

        return map;
    }

    private void forSeed(final Consumer<Integer> consumer) {
        for (int seed = 19; seed < 1119; seed += 119) {
            consumer.accept(seed);
        }
    }
}