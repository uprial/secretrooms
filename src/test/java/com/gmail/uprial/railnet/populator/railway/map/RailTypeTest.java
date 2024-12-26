package com.gmail.uprial.railnet.populator.railway.map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RailTypeTest {
    @Test
    public void testAll() {
        assertEquals(1, RailType.SURFACE.getHashCode());
        assertEquals(2, RailType.SURFACE.getMaxHashCode());

        assertEquals(2, RailType.UNDERGROUND.getHashCode());
        assertEquals(2, RailType.UNDERGROUND.getMaxHashCode());
    }
}