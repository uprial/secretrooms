package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.listeners.TurretCron;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import org.bukkit.block.BlockFace;

import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_BLOCK;
import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_SPACE;

public class EndMansionStop extends EndMansionChunk {
    EndMansionStop(final BlockFace blockFace) {
        super(blockFace);
    }

    @Override
    void populate(final VirtualChunk vc) {
        final int y = vc.getMinHeight() + 3;

        vc.box(END_MANSION_BLOCK,
                0, y, 1,
                3, y, 4);
        vc.box(END_MANSION_SPACE,
                0, y + 1, 1,
                3, y + 2, 4);

        /*
            The turrets are positioned so that
            a player can't take a position between them,
            causing them to kill each other:
            there is a base in between with high blast resistance.
         */
        TurretCron.spawn(vc, 1, y + 3, 2, TurretCron.TurretType.BIG);
    }

    @Override
    public String toString() {
        return "Stop";
    }
}