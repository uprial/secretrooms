package com.gmail.uprial.secretrooms.populator.endmansion;

import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.Map;

public class EndMansionCorner extends EndMansionChunk {
    EndMansionCorner(final BlockFace blockFace) {
        super(blockFace);
    }

    @Override
    void populate(final VirtualChunk vc) {
        {
            // Platform
            final int y = vc.getMinHeight() + 3;
            vc.box(Material.AIR,
                    0, y + 1, 0,
                    5, y + 5, 5);

            vc.box(Material.OBSIDIAN,
                    0, y, 1,
                    5, y, 4);

            for (int i = 0; i <= +5; i += 5) {
                vc.box(Material.OBSIDIAN,
                        1, y, i,
                        4, y, i);
            }
        }

        {
            final int y = vc.getMinHeight() + 7;

            for (int i = -1; i <= +1; i += 2) {
                // Vertical defence
                vc.set(2, y + i, 2, Material.OBSIDIAN);
                vc.set(3, y + i, 3, Material.OBSIDIAN);
                // Horizontal defence #1
                vc.set(2 + i, y, 2, Material.OBSIDIAN);
                vc.set(2, y, 2 + i, Material.OBSIDIAN);
            }
            // Horizontal defence #2
            vc.set(4, y, 3, Material.OBSIDIAN);
            vc.set(3, y, 4, Material.OBSIDIAN);

            for(final Map.Entry<EntityType,Integer> entry : ImmutableMap.<EntityType, Integer>builder()
                    .put(EntityType.WITCH, 2)
                    .put(EntityType.BREEZE, 3)
                    .build().entrySet()) {

                final EntityType entityType = entry.getKey();
                final Integer position = entry.getValue();

                final CreatureSpawner spawner
                        = (CreatureSpawner) vc.set(position, y, position, Material.SPAWNER).getState();

                // Spawn fewer entities
                spawner.setMaxNearbyEntities(8); // Default: 16
                spawner.setSpawnCount(2); // Default: 4

                spawner.setSpawnedType(entityType);

                spawner.update();
            }
        }
    }

    @Override
    public String toString() {
        return "Corner";
    }
}