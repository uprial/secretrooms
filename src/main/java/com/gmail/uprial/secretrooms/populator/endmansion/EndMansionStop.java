package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.listeners.TurretCron;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class EndMansionStop extends EndMansionChunk {
    EndMansionStop(final BlockFace blockFace) {
        super(blockFace);
    }

    @Override
    void populate(final VirtualChunk vc) {
        final int y = vc.getMinHeight() + 3;

        vc.box(Material.CRYING_OBSIDIAN,
                0, y, 1,
                3, y, 4);
        vc.box(Material.AIR,
                0, y + 1, 1,
                3, y + 2, 4);

        TurretCron.spawn(vc, 1, y + 3, 2);
    }

    @Override
    public String toString() {
        return "Stop";
    }
}