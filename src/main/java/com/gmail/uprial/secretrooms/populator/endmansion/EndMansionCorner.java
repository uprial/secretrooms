package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.populator.SpawnerHelper;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import com.google.common.collect.ImmutableMap;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;

import java.util.Map;

import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_BLOCK;
import static com.gmail.uprial.secretrooms.populator.endmansion.EndMansionMaterials.END_MANSION_SPACE;

public class EndMansionCorner extends EndMansionChunk {
    EndMansionCorner(final BlockFace blockFace) {
        super(blockFace);
    }

    @Override
    void populate(final VirtualChunk vc) {
        {
            // Platform
            final int y = vc.getMinHeight() + 3;
            vc.box(END_MANSION_SPACE,
                    0, y + 1, 0,
                    5, y + 5, 5);

            vc.box(END_MANSION_BLOCK,
                    0, y, 1,
                    5, y, 4);

            for (int i = 0; i <= +5; i += 5) {
                vc.box(END_MANSION_BLOCK,
                        1, y, i,
                        4, y, i);
            }
        }

        {
            final int y = vc.getMinHeight() + 7;

            for (int i = -1; i <= +1; i += 2) {
                // Vertical defence
                vc.set(2, y + i, 2, END_MANSION_BLOCK);
                vc.set(3, y + i, 3, END_MANSION_BLOCK);
                // Horizontal defence #1
                vc.set(2 + i, y, 2, END_MANSION_BLOCK);
                vc.set(2, y, 2 + i, END_MANSION_BLOCK);
            }
            // Horizontal defence #2
            vc.set(4, y, 3, END_MANSION_BLOCK);
            vc.set(3, y, 4, END_MANSION_BLOCK);

            for(final Map.Entry<EntityType,Integer> entry : ImmutableMap.<EntityType, Integer>builder()
                    .put(EntityType.WITCH, 2)
                    .put(EntityType.BREEZE, 3)
                    .build().entrySet()) {

                final Integer position = entry.getValue();

                new SpawnerHelper().set(vc.get(position, y, position), entry.getKey());
            }
        }
    }

    @Override
    public String toString() {
        return "Corner";
    }
}