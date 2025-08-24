package com.gmail.uprial.secretrooms.populator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class SpawnerHelper {
    private DistanceDensity distanceDensity = null;

    public SpawnerHelper() {
    }

    // Spawn entities quicker
    public SpawnerHelper setQuick(final DistanceDensity distanceDensity) {
        this.distanceDensity = distanceDensity;
        return this;
    }

    public void set(final Block block, final EntityType entityType) {
        block.setType(Material.SPAWNER, false);

        final CreatureSpawner spawner = (CreatureSpawner) block.getState();

        if(distanceDensity != null) {
            final int delimiter = distanceDensity.get(block, 19) + 1;

            spawner.setMinSpawnDelay(20 / delimiter); // Default: 200
            spawner.setMaxSpawnDelay(80 / delimiter); // Default: 800
        }

        // Spawn fewer entities
        spawner.setMaxNearbyEntities(8); // Default: 16
        spawner.setSpawnCount(8); // Default: 4

        // spawner.setDelay(-1);

        spawner.setSpawnedType(entityType);

        spawner.update();
    }
}
