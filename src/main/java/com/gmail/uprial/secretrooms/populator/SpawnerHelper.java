package com.gmail.uprial.secretrooms.populator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class SpawnerHelper {
    private boolean isQuick = false;

    public SpawnerHelper() {
    }

    // Spawn entities quicker
    public SpawnerHelper setQuick() {
        isQuick = true;
        return this;
    }

    public void set(final Block block, final EntityType entityType) {

        block.setType(Material.SPAWNER, false);

        final CreatureSpawner spawner = (CreatureSpawner) block.getState();

        if(isQuick) {
            spawner.setMinSpawnDelay(20); // Default: 200
            spawner.setMaxSpawnDelay(80); // Default: 800
        }
        // Spawn fewer entities
        spawner.setMaxNearbyEntities(8); // Default: 16
        spawner.setSpawnCount(8); // Default: 4

        // spawner.setDelay(-1);

        spawner.setSpawnedType(entityType);

        spawner.update();
    }
}
