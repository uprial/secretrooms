package com.gmail.uprial.railnet.populator.railway.schema;

import org.bukkit.block.Biome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiomeEnumTest {
    @Test
    public void testConsistency() {
        // Exclude Biome.CUSTOM
        assertEquals(BiomeEnum.values().length, Biome.class.getFields().length - 1);
    }
}