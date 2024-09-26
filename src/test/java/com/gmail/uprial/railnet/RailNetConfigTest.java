package com.gmail.uprial.railnet;

import com.gmail.uprial.railnet.config.InvalidConfigException;
import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class RailNetConfigTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyDebug() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'debug' flag. Use default value false");
        RailNetConfig.isDebugMode(getPreparedConfig(""), getDebugFearingCustomLogger());
    }

    @Test
    public void testNormalDebug() throws Exception {
        assertTrue(RailNetConfig.isDebugMode(getPreparedConfig("debug: true"), getDebugFearingCustomLogger()));
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty minecart max speed");
        loadConfig(getDebugFearingCustomLogger(), "");
    }

    @Test
    public void testWrongMinecartMaxSpeed() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A minecart max speed is not a double");
        loadConfig("minecart-max-speed: x");
    }

    @Test
    public void testEmptyMinecartSlowSpeed() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty minecart slow speed");
        loadConfig("minecart-max-speed: 4.0");
    }

    @Test
    public void testEmptyMinecartSlowBlock() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty minecart slow block");
        loadConfig("minecart-max-speed: 4.0",
                "minecart-slow-speed: 0.2");
    }

    @Test
    public void testNormalConfig() throws Exception {
        assertEquals(
                "minecart-max-speed: 4.00, " +
                        "minecart-slow-speed: 0.20, " +
                        "minecart-slow-block: GRAVEL",
                loadConfig("minecart-max-speed: 4.0",
                        "minecart-slow-speed: 0.2",
                        "minecart-slow-block: GRAVEL").toString());
    }
}