package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import org.bukkit.Chunk;
import org.bukkit.block.BlockFace;

abstract class EndMansionChunk {
    private final BlockFace blockFace;

    EndMansionChunk(final BlockFace blockFace) {
        this.blockFace = blockFace;
    }

    void populate(final Chunk chunk) {
        populate(new VirtualChunk(toString(), chunk, blockFace));
    }

    abstract void populate(final VirtualChunk vc);
}
