package com.gmail.uprial.railnet.schema;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.generator.structure.StructureType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StructureTypeEnumTest extends TestConfigBase {
    @Test
    public void testConsistency() throws Exception {
        assertEquals(StructureTypeEnum.values().length, StructureType.class.getFields().length);
    }
}