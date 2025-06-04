package com.gmail.uprial.railnet.populator.endmansion;

import com.gmail.uprial.railnet.populator.VirtualChunk;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class EndMansionRoad extends EndMansionChunk {

    EndMansionRoad(final BlockFace blockFace) {
        super(blockFace);
    }

    @Override
    public void populate(final VirtualChunk vc) {
        final int y = vc.getMinHeight() + 3;

        vc.box(Material.POLISHED_BLACKSTONE_BRICK_SLAB,
                0, y, 1,
                15, y, 4);
        vc.box(Material.AIR,
                0, y + 1, 1,
                15, y + 2, 4);
    }

    @Override
    public String toString() {
        return "Road";
    }
}