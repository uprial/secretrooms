package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.block.Block;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MineshaftPopulatorTest extends TestConfigBase {

    @Test
    public void testDistanceDensity() {
        assertEquals(0, getDD(0, 0, false));
        assertEquals(0, getDD(0, 0, true));

        assertEquals(0, getDD(5_000, 5_000, false));
        assertEquals(1, getDD(5_000, 5_000, true));

        assertEquals(0, getDD(10_000, 10_000, false));
        assertEquals(2, getDD(10_000, 10_000, true));
    }

    @Test
    public void testDistanceDensityDistribution() {
        final int r = 4_000;

        int chests = 0;
        int staticDensity = 0;
        int dynamicDensity = 0;
        for(int x = -r; x <= r; x += 100) {
            for(int z = -r; z <= r; z += 100) {
                chests++;
                staticDensity += getDD(x, z, false);
                dynamicDensity += getDD(x, z, true);
            }
        }

        assertEquals(6_561, chests);
        assertEquals(0, staticDensity);
        assertEquals(228, dynamicDensity);

        assertEquals(0.035D, 1.0D * dynamicDensity / chests, 0.01D);
    }

    private int getDD(final int x, final int z, final boolean dynamicLootDensity) {
        final Block block = mock(Block.class);

        when(block.getX()).thenReturn(x);
        when(block.getZ()).thenReturn(z);

        return new MineshaftPopulator(null, null, dynamicLootDensity)
                .getDistanceDensity(block);
    }
}