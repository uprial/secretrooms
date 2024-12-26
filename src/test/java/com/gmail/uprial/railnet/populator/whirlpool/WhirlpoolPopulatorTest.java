package com.gmail.uprial.railnet.populator.whirlpool;

import com.gmail.uprial.railnet.populator.ChunkXZ;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.gmail.uprial.railnet.populator.whirlpool.WhirlpoolPopulator.isAppropriate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WhirlpoolPopulatorTest {
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
}