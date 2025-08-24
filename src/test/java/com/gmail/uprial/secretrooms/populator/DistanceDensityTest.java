package com.gmail.uprial.secretrooms.populator;

import com.gmail.uprial.secretrooms.helpers.TestConfigBase;
import org.bukkit.block.Block;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DistanceDensityTest extends TestConfigBase {

    @Test
    public void testGet() {
        assertEquals(0, getDD(0, 0, 0));
        assertEquals(0, getDD(0, 0, 5_000));

        assertEquals(0, getDD(5_000, 5_000, 0));
        assertEquals(1, getDD(5_000, 5_000, 5_000));
        assertEquals(1, getDD(-5_000, -5_000, 5_000));

        assertEquals(0, getDD(10_000, 10_000, 0));
        assertEquals(2, getDD(10_000, 10_000, 5_000));
        assertEquals(2, getDD(-10_000, -10_000, 5_000));
    }

    @Test
    public void testExp() {
        assertEquals(0, getDD(2_000, 2_000, 5_000));
        assertEquals(0, getDD(3_000, 3_000, 5_000));
        assertEquals(1, getDD(4_000, 4_000, 5_000));
        assertEquals(1, getDD(5_000, 5_000, 5_000));

        assertEquals(0, getDD(0, 0, 5_000));
        assertEquals(1, getDD(5_000, 5_000, 5_000));
        assertEquals(2, getDD(10_000, 10_000, 5_000));
        assertEquals(2, getDD(15_000, 15_000, 5_000));
        assertEquals(3, getDD(20_000, 20_000, 5_000));
        assertEquals(3, getDD(25_000, 25_000, 5_000));
        assertEquals(4, getDD(30_000, 30_000, 5_000));
        assertEquals(4, getDD(35_000, 35_000, 5_000));
        assertEquals(5, getDD(40_000, 40_000, 5_000));
        assertEquals(5, getDD(45_000, 45_000, 5_000));
        assertEquals(5, getDD(50_000, 50_000, 5_000));
        assertEquals(6, getDD(55_000, 55_000, 5_000));
        assertEquals(6, getDD(60_000, 60_000, 5_000));
        assertEquals(6, getDD(65_000, 65_000, 5_000));
        assertEquals(7, getDD(70_000, 70_000, 5_000));
        assertEquals(7, getDD(75_000, 75_000, 5_000));
        assertEquals(8, getDD(80_000, 80_000, 5_000));
        assertEquals(8, getDD(85_000, 85_000, 5_000));
        assertEquals(8, getDD(90_000, 90_000, 5_000));
        assertEquals(8, getDD(95_000, 95_000, 5_000));
        assertEquals(9, getDD(100_000, 100_000, 5_000));
    }

    @Test
    public void testGetMax() {
        assertEquals(1, getDD(5_000, 5_000, 5_000));
        assertEquals(9, getDD(500_000, 500_000, 5_000));
    }

    @Test
    public void testDistribution() {
        final int r = 4_000;

        int chests = 0;
        int staticDensity = 0;
        int dynamicDensity = 0;
        for(int x = -r; x <= r; x += 100) {
            for(int z = -r; z <= r; z += 100) {
                chests++;
                staticDensity += getDD(x, z, 0);
                dynamicDensity += getDD(x, z, 5_000);
            }
        }

        assertEquals(6_561, chests);
        assertEquals(0, staticDensity);
        assertEquals(228, dynamicDensity);

        assertEquals(0.035D, 1.0D * dynamicDensity / chests, 0.01D);
    }

    private int getDD(final int x, final int z, final int multiplier) {
        final Block block = mock(Block.class);

        when(block.getX()).thenReturn(x);
        when(block.getZ()).thenReturn(z);

        return new DistanceDensity(multiplier).get(block, 9);
    }
}