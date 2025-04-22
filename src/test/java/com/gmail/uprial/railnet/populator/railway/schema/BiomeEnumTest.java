package com.gmail.uprial.railnet.populator.railway.schema;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.block.Biome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BiomeEnumTest extends TestConfigBase {
    @Test
    public void testConsistency() {
        // Exclude Biome.CUSTOM
        assertEquals(BiomeEnum.values().length, Biome.class.getFields().length - 1);
    }
}