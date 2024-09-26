package com.gmail.uprial.railnet.config;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.bukkit.Material;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.railnet.config.ConfigReaderMaterial.*;
import static org.junit.Assert.assertEquals;

public class ConfigReaderMaterialTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyMaterial() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty Material");
        getMaterial(getPreparedConfig(""), "material", "Material");
    }

    @Test
    public void testUnknownMaterial() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Unknown Material 'TEST'");
        getMaterial(getPreparedConfig("material: TEST"), "material", "Material");
    }

    @Test
    public void testWholeMaterial() throws Exception {
        Material material = getMaterial(getPreparedConfig("material: EGG"), "material", "Material");
        assertEquals(Material.EGG, material);
    }
}