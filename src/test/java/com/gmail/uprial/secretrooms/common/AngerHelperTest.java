package com.gmail.uprial.secretrooms.common;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AngerHelperTest {
    @Test
    public void testGetSmallestItem() {
        assertEquals(Long.valueOf(1L), AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .add(2L)
                        .add(1L)
                        .add(3L)
                        .build(), Double::valueOf));
    }

    @Test
    public void testNullSmallestItem() {
        assertNull(AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .build(), Double::valueOf));
    }

    @Test
    public void testNullSmallestItemValue() {
        assertNull(AngerHelper.getSmallestItem(
                ImmutableList.<Long>builder()
                        .add(2L)
                        .add(1L)
                        .add(3L)
                        .build(), (final Long l) -> null));
    }
}