package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.BlockSeed;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class ContentSeed extends BlockSeed {
    protected ContentSeed(long seed, long x, long z) {
        super(seed, x, z);
    }

    public static ContentSeed valueOf(final Block block) {
        // Sometimes, we need a different hash for a block.
        return new ContentSeed(block.getWorld().getSeed(),
                block.getX(),
                block.getY());
    }

    public static ContentSeed valueOf(final Entity entity) {
        // Sometimes, we need a different hash for an entity.
        return new ContentSeed(entity.getWorld().getSeed(),
                entity.getLocation().getBlockX(),
                entity.getLocation().getBlockY());
    }
}
