package com.gmail.uprial.secretrooms.populator;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class AbstractSeedSpecificPopulatorTest {
    static class TestBlockSeed extends BlockSeed {
        TestBlockSeed(final long seed, final long x, final long z) {
            super(seed, x, z);
        }
    }

    @Test
    public void testConsistency() {
        for(int probability = 88; probability < 288; probability += 22) {
            for (int seed = 19; seed < 1119; seed += 119) {

                Map<ChunkXZ, Boolean> prevMap = null;
                for (int i = 0; i < 2; i++) {
                    final Map<ChunkXZ, Boolean> map = new HashMap<>();
                    for (int x = -99; x < 100; x++) {
                        for (int z = -99; z < 100; z++) {
                            map.put(new ChunkXZ(x, z), isAppropriate(seed, x, z, probability));
                        }
                    }

                    if (i == 0) {
                        assertNull(prevMap);
                        prevMap = map;
                    } else {
                        assertNotNull(prevMap);
                        assertEquals(prevMap, map);
                    }
                }
            }
        }
    }

    @Test
    public void testDistribution() {
        for(int probability = 88; probability < 288; probability += 22) {
            for (int seed = 19; seed < 1119; seed += 119) {
                int counter = 0;
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(seed, x, z, probability)) {
                            counter++;
                        }
                    }
                }

                final int frequency = 200 * 200 / probability;
                assertTrue(counter >= frequency / 2);
                assertTrue(counter <= frequency * 2);
            }
        }
    }

    @Test
    public void testAsymmetry() {
        for (int probability = 1288; probability < 2288; probability += 128) {
            for (int seed = 119; seed < 219; seed += 19) {

                final Set<ChunkXZ> appropriates = new HashSet<>();
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(seed, x, z, probability)) {
                            appropriates.add(new ChunkXZ(x, z));
                        }
                    }
                }

                assertFalse(String.format("Appropriates is empty for %d/%d",
                                probability, seed),
                        appropriates.isEmpty());
                assertFalse(String.format("Too many appropriates for %d/%d: %d/%d",
                                probability, seed, appropriates.size(), probability / 5),
                        appropriates.size() > probability / 5);

                for (final ChunkXZ a : appropriates) {
                    assertTrue(String.format("%s is self-symmetric", a),
                            appropriates.contains(new ChunkXZ(+a.getX(), +a.getZ())));

                    final Set<ChunkXZ> symmetric = new HashSet<>();
                    // Simple sign inversion
                    for(int x = -1; x <= +1; x+=2) {
                        for (int z = -1; z <= +1; z+=2) {
                            if ((x != +1) && (z != +1)) {
                                final ChunkXZ chunkXZ = new ChunkXZ(x * a.getX(), z * a.getZ());
                                if (!a.equals(chunkXZ) && (appropriates.contains(chunkXZ))) {
                                    symmetric.add(chunkXZ);
                                }
                            }
                        }
                    }
                    // Sign and value inversion
                    for(int x = -1; x <= +1; x+=2) {
                        for (int z = -1; z <= +1; z+=2) {
                            final ChunkXZ chunkXZ = new ChunkXZ(x * a.getZ(), z * a.getX());
                            if (!a.equals(chunkXZ) && (appropriates.contains(chunkXZ))) {
                                symmetric.add(chunkXZ);
                            }
                        }
                    }
                    assertTrue(String.format("%s symmetry for %d/%d: %s", a, probability, seed, symmetric),
                            symmetric.isEmpty());
                }
            }
        }
    }

    @Test
    public void testPrimeNumbers() {
        // My guess was that the prime numbers are more stable, but they are not
        final Map<Integer,Integer> probability2count = new LinkedHashMap<>();
        probability2count.put(97, 4039);
        probability2count.put(100, 4004);
        probability2count.put(101, 3806);

        probability2count.put(293, 1293);
        probability2count.put(300, 1326);
        probability2count.put(307, 1365);

        for(final Map.Entry<Integer,Integer> entry : probability2count.entrySet()) {
            final int probability = entry.getKey();
            Integer counter = 0;

            for (int seed = 19; seed < 1119; seed += 119) {
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(seed, x, z, probability)) {
                            counter++;
                        }
                    }
                }
            }
            assertEquals(String.format("Prime number %d counter", probability),
                    entry.getValue(), counter);
        }
    }

    private boolean isAppropriate(final long seed, final int x, final int z, final int probability) {
        return AbstractSeedSpecificPopulator.isAppropriate(new TestBlockSeed(seed, x, z), probability);
    }
}