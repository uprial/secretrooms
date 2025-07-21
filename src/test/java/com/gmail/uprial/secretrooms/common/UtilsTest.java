package com.gmail.uprial.secretrooms.common;

import com.google.common.collect.Lists;
import org.junit.Test;

import static com.gmail.uprial.secretrooms.common.Utils.*;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
    @Test
    public void testSeconds2ticks() {
        assertEquals(40, seconds2ticks(2));
    }

    @Test
    public void testJoinEmptyStrings() {
        assertEquals("", joinStrings(",", Lists.newArrayList(new String[]{})));
    }

    @Test
    public void testJoinOneString() {
        assertEquals("a", joinStrings(",", Lists.newArrayList("a")));
    }

    @Test
    public void testJoinSeveralStrings() {
        assertEquals("a,b", joinStrings(",", Lists.newArrayList("a", "b")));
    }

    @Test
    public void testGetFormattedTicks_S() {
        assertEquals("1s", getFormattedTicks(20));
        assertEquals("1,000s", getFormattedTicks(20_000));
        assertEquals("61s", getFormattedTicks(20 * 61));
        assertEquals("3,601s", getFormattedTicks(20 * 3_601));
    }

    @Test
    public void testGetFormattedTicks_M() {
        assertEquals("1m", getFormattedTicks(20 * 60));
        assertEquals("1,000m", getFormattedTicks(20 * 60_000));
        assertEquals("61m", getFormattedTicks(20 * 3_660));
    }

    @Test
    public void testGetFormattedTicks_H() {
        assertEquals("1h", getFormattedTicks(20 * 3_600));
        assertEquals("1,000h", getFormattedTicks(20 * 3_600_000));
        assertEquals("25h", getFormattedTicks(20 * (86_400 + 3_600)));
    }

    @Test
    public void testGetFormattedTicks_D() {
        assertEquals("1d", getFormattedTicks(20 * 86_400));
        assertEquals("1,000d", getFormattedTicks(20 * 86_400_000));
    }

    @Test
    public void testGetFormattedTicks_RealCases() {
        assertEquals("0", getFormattedTicks(0));

        assertEquals("15m", getFormattedTicks(seconds2ticks(60 * 15)));
        assertEquals("1h", getFormattedTicks(seconds2ticks(3_600)));
        assertEquals("4h", getFormattedTicks(seconds2ticks(3_600 * 4)));
        assertEquals("1d", getFormattedTicks(seconds2ticks(3_600 * 24)));
        assertEquals("7d", getFormattedTicks(seconds2ticks(3_600 * 24 * 7)));

        assertEquals("1m", getFormattedTicks(seconds2ticks(60)));
        assertEquals("5m", getFormattedTicks(seconds2ticks(60 * 5)));
        assertEquals("15m", getFormattedTicks(seconds2ticks(60 * 15)));
        assertEquals("1h", getFormattedTicks(seconds2ticks(3_600)));
        assertEquals("4h", getFormattedTicks(seconds2ticks(3_600 * 4)));
    }
}
