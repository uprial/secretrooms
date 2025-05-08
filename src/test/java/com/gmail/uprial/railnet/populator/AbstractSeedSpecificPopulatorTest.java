package com.gmail.uprial.railnet.populator;

import org.junit.Test;

import java.util.*;

import static com.gmail.uprial.railnet.populator.AbstractSeedSpecificPopulator.isAppropriate;
import static org.junit.Assert.*;

public class AbstractSeedSpecificPopulatorTest {
    @Test
    public void testConsistency() {
        final int seed = 119;
        final int density = 12;

        final Map<Integer, Object> maps = new HashMap<>();
        for(int i = 0; i < 2; i++) {
            final Map<ChunkXZ, Boolean> map = new HashMap<>();
            for(int x = -99; x < 100; x++) {
                for(int z = -99; z < 100; z++) {
                    map.put(new ChunkXZ(x, z), isAppropriate(x, z, seed, density));
                }
            }

            if(i > 0) {
                assertEquals(map, maps.get(i - 1));
            }

            maps.put(i, map);
        }
    }

    @Test
    public void testDistribution() {
        for(int density = 88; density < 288; density += 22) {
            for (int seed = 19; seed < 1119; seed += 119) {
                int counter = 0;
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(x, z, seed, density)) {
                            counter++;
                        }
                    }
                }

                final int frequency = 200 * 200 / density;
                assertTrue(counter >= frequency / 2);
                assertTrue(counter <= frequency * 2);
            }
        }
    }

    @Test
    public void testAsymmetry() {
        for(long density = 1288; density < 2288; density += 128) {
            for (long seed = 119; seed < 219; seed += 19) {

                final Set<ChunkXZ> appropriates = new HashSet<>();
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(x, z, seed, density)) {
                            appropriates.add(new ChunkXZ(x, z));
                        }
                    }
                }

                assertFalse(String.format("Appropriates is empty for %d/%d", density, seed),
                        appropriates.isEmpty());
                assertFalse(String.format("Too many appropriates for %d/%d: %d/%d", density, seed, appropriates.size(), density / 5),
                        appropriates.size() > density / 5);

                for (final ChunkXZ a : appropriates) {
                    assertTrue(String.format("%s is self-symmetric", a),
                            appropriates.contains(new ChunkXZ(+a.getX(), +a.getZ())));

                    final Set<ChunkXZ> symmetric = new HashSet<>();
                    // Simple sign inversion
                    for(int x = -1; x <= +1; x+=2) {
                        for (int z = -1; z <= +1; z+=2) {
                            if ((x != +1) && (z != +1)) {
                                final ChunkXZ chunkXZ = new ChunkXZ(x * a.getX(), z * a.getZ());
                                if (appropriates.contains(chunkXZ)) {
                                    symmetric.add(chunkXZ);
                                }
                            }
                        }
                    }
                    // Sign and value inversion
                    for(int x = -1; x <= +1; x+=2) {
                        for (int z = -1; z <= +1; z+=2) {
                            final ChunkXZ chunkXZ = new ChunkXZ(x * a.getZ(), z * a.getX());
                            if (appropriates.contains(chunkXZ)) {
                                symmetric.add(chunkXZ);
                            }
                        }
                    }
                    assertTrue(String.format("%s symmetry for %d/%d: %s", a, density, seed, symmetric),
                            symmetric.size() < 2);
                }
            }
        }
    }

    @Test
    public void testPrimeNumbers() {
        // My guess was that the prime numbers are more stable, but they are not
        Map<Integer,Integer> density2count = new LinkedHashMap<>();
        density2count.put(97, 4298);
        density2count.put(100, 3982);
        density2count.put(101, 3831);

        density2count.put(293, 1381);
        density2count.put(300, 1312);
        density2count.put(307, 1341);

        for(Map.Entry<Integer,Integer> entry : density2count.entrySet()) {
            int density = entry.getKey();
            Integer counter = 0;

            for (int seed = 19; seed < 1119; seed += 119) {
                for (int x = -99; x < 100; x++) {
                    for (int z = -99; z < 100; z++) {
                        if (isAppropriate(x, z, seed, density)) {
                            counter++;
                        }
                    }
                }
            }
            assertEquals(counter, entry.getValue());
        }
    }
}