package com.gmail.uprial.secretrooms.populator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ChunkXZTest {
    @Test
    public void testAll() {
        final ChunkXZ chunkXZ = new ChunkXZ(1, 2);

        assertEquals("1:2", chunkXZ.toString());

        assertEquals(1, chunkXZ.getX());
        assertEquals(2, chunkXZ.getZ());

        assertEquals("3:0", chunkXZ.getSum(new ChunkXZ(2, -2)).toString());

        assertEquals(1002, chunkXZ.hashCode());

        assertEquals(chunkXZ, new ChunkXZ(1, 2));
        assertNotEquals(chunkXZ, new ChunkXZ(1, 3));
        assertNotEquals(chunkXZ, new ChunkXZ(2, 2));
    }
}