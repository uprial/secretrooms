package com.gmail.uprial.railnet.populator.railway.schema;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.generator.structure.Structure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StructureEnumTest extends TestConfigBase {
    @Test
    public void testConsistency() {
        assertEquals(StructureEnum.values().length, Structure.class.getFields().length);
    }
}