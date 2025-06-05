package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.populator.ChunkXZ;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndMansionPopulatorTest {
    private static final long TEST_SEED = -1565193744182814265L;

    @Test
    public void testCoordinatesDistribution() {
        final World world = mock(World.class);
        when(world.getSeed()).thenReturn(TEST_SEED);

        final Chunk chunk = mock(Chunk.class);
        when(chunk.getWorld()).thenReturn(world);

        final EndMansionPopulator cp = new EndMansionPopulator(null);

        for(Map.Entry<Integer, ChunkXZ> entry : ImmutableMap.<Integer, ChunkXZ>builder()
                .put(1, new ChunkXZ(-60, 15))
                .put(2, new ChunkXZ(-52, 112))
                .put(3, new ChunkXZ(-74, 170))
                .put(4, new ChunkXZ(-53, 242))
                .put(5, new ChunkXZ(32, 308))
                .put(6, new ChunkXZ(55, 367))
                .build().entrySet()) {

            final int x = cp.getX(entry.getKey(), chunk);
            final int z = cp.getZ(entry.getKey(), x);

            assertEquals(String.format("Coordinates for step %d", entry.getKey()),
                    entry.getValue(), new ChunkXZ(x, z));
        }
    }
}