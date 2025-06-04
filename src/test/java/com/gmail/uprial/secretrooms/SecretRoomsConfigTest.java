package com.gmail.uprial.secretrooms;

import com.gmail.uprial.secretrooms.config.InvalidConfigException;
import com.gmail.uprial.secretrooms.helpers.TestConfigBase;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class SecretRoomsConfigTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyDebug() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'debug' flag. Use default value false");
        SecretRoomsConfig.isDebugMode(getPreparedConfig(""), getDebugFearingCustomLogger());
    }

    @Test
    public void testNormalDebug() throws Exception {
        assertTrue(SecretRoomsConfig.isDebugMode(getPreparedConfig("debug: true"), getDebugFearingCustomLogger()));
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'distance-density-multiplier' value");
        loadConfig(getDebugFearingCustomLogger(), "");
    }

    @Test
    public void testNotMap() throws Exception {
        e.expect(InvalidConfigurationException.class);
        e.expectMessage("Top level is not a Map.");
        loadConfig("x");
    }

    @Test
    public void testNormalConfig() throws Exception {
        assertEquals(
                "distance-density-multiplier: 5,000",
                loadConfig(getCustomLogger(),
                        "distance-density-multiplier: 5_000").toString());
    }
}