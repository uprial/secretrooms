package com.gmail.uprial.secretrooms.populator.endmansion;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndMansionPopulatorTest {
    private static final long TEST_SEED = -1565193744182814265L;

    @Test
    public void testInitialCoordinates() {
        final World world = mock(World.class);
        when(world.getSeed()).thenReturn(TEST_SEED);

        final Chunk chunk = mock(Chunk.class);
        when(chunk.getWorld()).thenReturn(world);

        final EndMansionPopulator cp = new EndMansionPopulator(null);

        final int x = cp.getX(1, chunk);
        final int z = cp.getZ(1, x);

        assertEquals(-60, x);
        assertEquals(15, z);
    }
}