package com.gmail.uprial.railnet.schema;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.generator.structure.Structure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StructureEnumTest extends TestConfigBase {
    @Test
    public void testConsistency() throws Exception {
        assertEquals(StructureEnum.values().length, Structure.class.getFields().length);
    }
}