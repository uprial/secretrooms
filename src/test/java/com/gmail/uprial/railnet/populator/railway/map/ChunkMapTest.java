package com.gmail.uprial.railnet.populator.railway.map;

import org.bukkit.block.BlockFace;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ChunkMapTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testOrdinalRailWay() throws Exception {
        final ChunkMap.OrdinalRailWay ordinalRailWay = new ChunkMap.OrdinalRailWay(1, RailType.UNDERGROUND, BlockFace.NORTH);

        assertEquals("UNDERGROUND-NORTH#1", ordinalRailWay.toString());
        assertEquals(RailType.UNDERGROUND, ordinalRailWay.getRailType());
        assertEquals(BlockFace.NORTH, ordinalRailWay.getBlockFace());

        assertFalse(ordinalRailWay.isAnotherWayOfTheSameType(new ChunkMap.OrdinalRailWay(1, RailType.UNDERGROUND, BlockFace.NORTH)));
        assertFalse(ordinalRailWay.isAnotherWayOfTheSameType(new ChunkMap.OrdinalRailWay(1, RailType.SURFACE, BlockFace.NORTH)));
        assertTrue(ordinalRailWay.isAnotherWayOfTheSameType(new ChunkMap.OrdinalRailWay(2, RailType.UNDERGROUND, BlockFace.NORTH)));

        assertEquals(383, ordinalRailWay.hashCode());

        assertEquals(ordinalRailWay, new ChunkMap.OrdinalRailWay(1, RailType.UNDERGROUND, BlockFace.NORTH));
        assertNotEquals(ordinalRailWay, new ChunkMap.OrdinalRailWay(2, RailType.UNDERGROUND, BlockFace.NORTH));
        assertNotEquals(ordinalRailWay, new ChunkMap.OrdinalRailWay(1, RailType.SURFACE, BlockFace.NORTH));
    }

    @Test
    public void testGetBlockFace() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");

        assertEquals(BlockFace.EAST, chunkMap.getBlockFace(1, 0));
        assertEquals(BlockFace.NORTH, chunkMap.getBlockFace(0, -1));
        assertEquals(BlockFace.WEST, chunkMap.getBlockFace(-1, 0));
        assertEquals(BlockFace.SOUTH, chunkMap.getBlockFace(0, 1));
    }

    @Test
    public void testGetWrongBlockFace() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");

        e.expect(InternalConfigurationError.class);
        e.expectMessage("Wrong block face mod 1-2 in home2hell");

        chunkMap.getBlockFace(1, 2);
    }

    @Test
    public void testEmpty() throws Exception {
        assertEquals(
                "home2hell{}",
                new ChunkMap("home2hell").toString());
    }

    @Test
    public void testAddWayInsideChunk() throws Exception {
        e.expect(InvalidMapException.class);
        e.expectMessage("Zero-length way from 0-0 to 0-0 in home2hell");
        new ChunkMap("home2hell").addWay(0, 0, 0, 0, RailType.UNDERGROUND);
    }

    @Test
    public void testAddWayX() throws Exception {
        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1], 1-0=[UNDERGROUND-EAST#1]}",
                new ChunkMap("home2hell").addWay(0, 0, 1, 0, RailType.UNDERGROUND).toString());
    }

    @Test
    public void testAddWayZ() throws Exception {
        assertEquals(
                "home2hell{0-0=[UNDERGROUND-SOUTH#1], 0-1=[UNDERGROUND-SOUTH#1]}",
                new ChunkMap("home2hell").addWay(0, 0, 0, 1, RailType.UNDERGROUND).toString());
    }

    @Test
    public void testAddWayXZ() throws Exception {
        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1], 1-0=[UNDERGROUND-EAST#1], 1-1=[UNDERGROUND-SOUTH#1]}",
                new ChunkMap("home2hell").addWay(0, 0, 1, 1, RailType.UNDERGROUND).toString());
    }

    @Test
    public void testAddWayXZLong() throws Exception {
        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1], 1-0=[UNDERGROUND-EAST#1], 2-0=[UNDERGROUND-EAST#1]," +
                        " 3-0=[UNDERGROUND-EAST#1], 3-1=[UNDERGROUND-SOUTH#1]}",
                new ChunkMap("home2hell").addWay(0, 0, 3, 1, RailType.UNDERGROUND).toString());
    }

    @Test
    public void testAddTwoWaysSameTypeSameWaySameId() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        final int initialWayId = chunkMap.wayId;
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        e.expect(InvalidMapException.class);
        e.expectMessage("Chunk 0-0 already contains the same type UNDERGROUND in home2hell");

        chunkMap.wayId = initialWayId;
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);
    }

    @Test
    public void testAddTwoWaysSameTypeSameWay() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        e.expect(InvalidMapException.class);
        e.expectMessage("Chunk 0-0 already contains the same type UNDERGROUND in home2hell");

        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);
    }

    @Test
    public void testAddTwoWaysDifferentTypeSameWay() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2], 1-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 2-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2], 3-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 3-1=[UNDERGROUND-SOUTH#1, SURFACE-SOUTH#2]}",
                chunkMap.addWay(0, 0, 3, 1, RailType.SURFACE).toString());
    }

    @Test
    public void testAddTwoWaysSameTypeTooCloseInitially() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        e.expect(InvalidMapException.class);
        e.expectMessage("Chunk 1-1 is too close to another way 1-0 of the same type UNDERGROUND in home2hell");

        chunkMap.addWay(1, 1, 3, 1, RailType.UNDERGROUND);
    }

    @Test
    public void testAddTwoWaysSameTypeTooCloseFinally() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        e.expect(InvalidMapException.class);
        e.expectMessage("Chunk 3-2 is too close to another way 3-1 of the same type UNDERGROUND in home2hell");

        chunkMap.addWay(2, 2, 3, 1, RailType.UNDERGROUND);
    }

    @Test
    public void testAddTwoWaysSameTypeDifferentWay() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1], 1-0=[UNDERGROUND-EAST#1], 2-0=[UNDERGROUND-EAST#1]," +
                        " 3-0=[UNDERGROUND-EAST#1], 3-1=[UNDERGROUND-SOUTH#1]," +
                        " 0--2=[UNDERGROUND-EAST#2], 1--2=[UNDERGROUND-EAST#2], 2--2=[UNDERGROUND-EAST#2]," +
                        " 3--2=[UNDERGROUND-EAST#2], 3--3=[UNDERGROUND-NORTH#2]}",
                chunkMap.addWay(0, -2, 3, -3, RailType.UNDERGROUND).toString());
    }

    @Test
    public void testAddTwoWaysDifferentTypeDifferentWay() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1], 1-0=[UNDERGROUND-EAST#1], 2-0=[UNDERGROUND-EAST#1]," +
                        " 3-0=[UNDERGROUND-EAST#1], 3-1=[UNDERGROUND-SOUTH#1]," +
                        " 0--2=[SURFACE-EAST#2], 1--2=[SURFACE-EAST#2], 2--2=[SURFACE-EAST#2]," +
                        " 3--2=[SURFACE-EAST#2], 3--3=[SURFACE-NORTH#2]}",
                chunkMap.addWay(0, -2, 3, -3, RailType.SURFACE).toString());
    }

    @Test
    public void testAddTwoWaysDifferentTypeSimilarWay() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        assertEquals(
                "home2hell{0-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 1-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 2-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 3-0=[UNDERGROUND-EAST#1, SURFACE-EAST#2]," +
                        " 3-1=[UNDERGROUND-SOUTH#1]," +
                        " 3--1=[SURFACE-NORTH#2]}",
                chunkMap.addWay(0, 0, 3, -1, RailType.SURFACE).toString());
    }

    @Test
    public void testDoesNoContainRailWays() throws Exception {
        assertFalse(new ChunkMap("home2hell").containsRailWays(0, 0));
    }

    @Test
    public void testGetRailWaysOne() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);

        assertFalse(chunkMap.containsRailWays(0, -1));
        assertEquals("{UNDERGROUND=EAST}", getMap(chunkMap, 0, 0).toString());
        assertEquals("{UNDERGROUND=EAST}", getMap(chunkMap,1, 0).toString());
        assertEquals("{UNDERGROUND=EAST}", getMap(chunkMap,2, 0).toString());
        assertEquals("{UNDERGROUND=EAST}", getMap(chunkMap, 3, 0).toString());
        assertEquals("{UNDERGROUND=SOUTH}", getMap(chunkMap, 3, 1).toString());
        assertFalse(chunkMap.containsRailWays(0, 4));

        assertEquals(5, forEachCounter(chunkMap));
    }

    @Test
    public void testGetRailWaysTwo() throws Exception {
        final ChunkMap chunkMap = new ChunkMap("home2hell");
        chunkMap.addWay(0, 0, 3, 1, RailType.UNDERGROUND);
        chunkMap.addWay(0, 0, 3, -1, RailType.SURFACE);

        assertFalse(chunkMap.containsRailWays(0, -1));
        for(int x = 0; x <= 2; x++) {
            assertEquals(BlockFace.EAST, getMap(chunkMap, x, 0).get(RailType.UNDERGROUND));
            assertEquals(BlockFace.EAST, getMap(chunkMap, x, 0).get(RailType.SURFACE));
        }
        assertEquals("{UNDERGROUND=EAST, SURFACE=EAST}", getMap(chunkMap, 3, 0).toString());
        assertEquals("{UNDERGROUND=SOUTH}", getMap(chunkMap, 3, 1).toString());
        assertEquals("{SURFACE=NORTH}", getMap(chunkMap, 3, -1).toString());
        assertFalse(chunkMap.containsRailWays(0, 4));

        assertEquals(10, forEachCounter(chunkMap));
    }

    private Map<RailType,BlockFace> getMap(final ChunkMap chunkMap, final int x, final int z) {
        final Map<RailType,BlockFace> map = new LinkedHashMap<>();
        chunkMap.forEach(x, z, map::put);

        return map;
    }

    private int forEachCounter(final ChunkMap chunkMap) {
        final AtomicInteger counter = new AtomicInteger(0);
        chunkMap.forEach((final int x, final int z, final RailType railType, final BlockFace blockFace) -> {
            counter.getAndIncrement();
        });

        return counter.intValue();
    }
}