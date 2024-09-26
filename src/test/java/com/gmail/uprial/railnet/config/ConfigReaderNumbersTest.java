package com.gmail.uprial.railnet.config;

import com.gmail.uprial.railnet.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.railnet.config.ConfigReaderNumbers.*;
import static org.junit.Assert.*;

public class ConfigReaderNumbersTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    // ==== getDouble ====

    @Test
    public void testBadMin() throws Exception {
        e.expect(InternalConfigurationError.class);
        e.expectMessage("Min value of double value has too many digits");
        getDouble(getPreparedConfig(""), getDebugFearingCustomLogger(), "d", "double value", 0.00001, 100, 0);
    }

    @Test
    public void testBadMax() throws Exception {
        e.expect(InternalConfigurationError.class);
        e.expectMessage("Max value of double value has too many digits");
        getDouble(getPreparedConfig(""), getDebugFearingCustomLogger(), "d", "double value", 0, 100.00001, 0);
    }

    @Test
    public void testBadDefault() throws Exception {
        e.expect(InternalConfigurationError.class);
        e.expectMessage("Default value of double value has too many digits");
        getDouble(getPreparedConfig(""), getDebugFearingCustomLogger(), "d", "double value", 0, 100, 0.00001);
    }

    @Test
    public void testEmptyDouble() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty double value. Use default value 0");
        getDouble(getPreparedConfig(""), getDebugFearingCustomLogger(), "d", "double value", 0, 100, 0);
    }

    @Test
    public void testEmptyDoubleWithoutDefault() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty double value");
        getDouble(getPreparedConfig(""), getCustomLogger(), "d", "double value", 0, 100);
    }

    @Test
    public void testWrongDouble() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A double value is not a double");
        getDouble(getPreparedConfig("n: 1.0.0"), getParanoiacCustomLogger(), "n", "double value", 0, 100, 0);
    }

    @Test
    public void testNormalDouble() throws Exception {
        assertEquals(50, getDouble(getPreparedConfig("n: 50"), getParanoiacCustomLogger(), "n", "double value", 0, 100, 0), Double.MIN_VALUE);
    }

    @Test
    public void testNormalIntDouble() throws Exception {
        assertEquals(50, getDouble(getPreparedConfig("n: 50"), getParanoiacCustomLogger(), "n", "double value", 0, 100, 0), Double.MIN_VALUE);
    }

    @Test
    public void testDoubleMinMaxConflict() throws Exception {
        e.expect(InternalConfigurationError.class);
        e.expectMessage("Max value of value number is greater than max value");
        getDouble(getPreparedConfig(""), getParanoiacCustomLogger(), "n", "value number", 200, 100, 0);
    }

    // ==== checkDoubleValue ====

    @Test
    public void testSmallDouble() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A double value should be at least 0");
        checkDoubleValue("double value", 0, 100, -1);
    }

    @Test
    public void testBigDouble() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A double value should be at most 100");
        checkDoubleValue("double value", 0, 100, 1000);
    }

    @Test
    public void testBigLeftPart() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A left part of double value has too many digits");
        checkDoubleValue("double value", 0, 100, 123456789012.0001);
    }

    @Test
    public void testBigRightPart() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A right part of double value has too many digits");
        checkDoubleValue("double value", 0, 100, 12345678901.00001);
    }
}