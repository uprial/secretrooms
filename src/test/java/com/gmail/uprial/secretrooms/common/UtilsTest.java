package com.gmail.uprial.secretrooms.common;

import com.google.common.collect.Lists;
import org.junit.Test;

import static com.gmail.uprial.secretrooms.common.Utils.*;
import static org.junit.Assert.assertEquals;

public class UtilsTest {
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
}