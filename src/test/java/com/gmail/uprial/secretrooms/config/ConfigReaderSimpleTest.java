package com.gmail.uprial.secretrooms.config;

import com.gmail.uprial.secretrooms.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.secretrooms.config.ConfigReaderSimple.*;
import static org.junit.Assert.*;

public class ConfigReaderSimpleTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    // ==== getString ====
    @Test
    public void testNullString() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Null string");
        getString(getPreparedConfig("s:"), "s", "string");
    }

    @Test
    public void testEmptyString() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty string");
        getString(getPreparedConfig("s: ''"), "s", "string");
    }

    @Test
    public void testNormalString() throws Exception {
        assertEquals("val", getString(getPreparedConfig("s: val"), "s", "string"));
    }

    // ==== getBoolean ====
    @Test
    public void testEmptyBoolean() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'value' flag. Use default value false");
        getBoolean(getPreparedConfig(""), getDebugFearingCustomLogger(), "f", "'value' flag", false);
    }

    @Test
    public void testEmptyBooleanDefaultValue() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'value' flag. Use default value true");
        getBoolean(getPreparedConfig(""), getDebugFearingCustomLogger(), "f", "'value' flag", true);
    }

    @Test
    public void testEmptyBooleanNoDefaultValue() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'value' flag");
        getBoolean(getPreparedConfig(""), getDebugFearingCustomLogger(), "f", "'value' flag");
    }

    @Test
    public void testInvalidBoolean() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'value' flag");
        getBoolean(getPreparedConfig("f: x"), getParanoiacCustomLogger(), "f", "'value' flag", false);
    }

    @Test
    public void testBooleanTrue() throws Exception {
        assertTrue(getBoolean(getPreparedConfig("f: true"), getParanoiacCustomLogger(), "f", "'value' flag", true));
    }

    @Test
    public void testBooleanTrueDifferentCase() throws Exception {
        assertTrue(getBoolean(getPreparedConfig("f: True"), getParanoiacCustomLogger(), "f", "'value' flag", true));
    }

    @Test
    public void testBooleanFalseDifferentCase() throws Exception {
        assertFalse(getBoolean(getPreparedConfig("f: False"), getParanoiacCustomLogger(), "f", "'value' flag", true));
    }
}