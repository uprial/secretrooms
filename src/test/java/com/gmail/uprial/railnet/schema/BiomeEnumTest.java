package com.gmail.uprial.railnet.schema;

import org.bukkit.block.Biome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiomeEnumTest {
    @Test
    public void testConsistency() throws Exception {
        // Exclude Biome.CUSTOM
        assertEquals(BiomeEnum.values().length, Biome.class.getFields().length - 1);
    }
}