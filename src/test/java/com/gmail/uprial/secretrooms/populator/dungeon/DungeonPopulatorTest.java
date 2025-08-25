package com.gmail.uprial.secretrooms.populator.dungeon;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import com.gmail.uprial.secretrooms.helpers.TestConfigBase;
import com.gmail.uprial.secretrooms.populator.ContentSeed;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DungeonPopulatorTest extends TestConfigBase {
    private static final long TEST_SEED = -1565193744182814265L;

    private Block block;

    @Before
    public void setUp() {
        super.setUp();

        final World world = mock(World.class);
        when(world.getSeed()).thenReturn(TEST_SEED);

        block = mock(Block.class);
        when(block.getWorld()).thenReturn(world);
    }

    @After
    public void tearDown() {
        block = null;

        super.tearDown();
    }

    @Test
    public void test1() {
        when(block.getX()).thenReturn(-2663);
        when(block.getY()).thenReturn(23);
        when(block.getZ()).thenReturn(-531);

        final BlockSeed bs = BlockSeed.valueOf(block);
        final ContentSeed cs = ContentSeed.valueOf(block);

        // before WOLF_ARMOR was added: LIGHT_GRAY_DYE
        assertEquals(Material.GOLD_BLOCK, bs.oneOf(DungeonPopulator.CHEST_LOOT_TABLE).get(0).getMaterial(cs));
    }

    @Test
    public void test2() {
        when(block.getX()).thenReturn(1282);
        when(block.getY()).thenReturn(30);
        when(block.getZ()).thenReturn(132);

        final BlockSeed bs = BlockSeed.valueOf(block);
        final ContentSeed cs = ContentSeed.valueOf(block);

        // before WOLF_ARMOR was added: PURPLE_DYE
        assertEquals(Material.DIAMOND_SWORD, bs.oneOf(DungeonPopulator.CHEST_LOOT_TABLE).get(0).getMaterial(cs));
    }

    @Test
    public void test3() {
        when(block.getX()).thenReturn(-110);
        when(block.getY()).thenReturn(25);
        when(block.getZ()).thenReturn(1133);

        final BlockSeed bs = BlockSeed.valueOf(block);
        final ContentSeed cs = ContentSeed.valueOf(block);

        // before WOLF_ARMOR was added: RED_DYE
        assertEquals(Material.OAK_LOG, bs.oneOf(DungeonPopulator.CHEST_LOOT_TABLE).get(0).getMaterial(cs));
    }
}