package com.gmail.uprial.secretrooms.populator.loot;

import com.gmail.uprial.secretrooms.populator.SpawnerHelper;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Illusioner;
import org.bukkit.util.Vector;

public class EndShipBlockBrewingStandHelper {
    private final Block block;

    EndShipBlockBrewingStandHelper(final Block block) {
        this.block = block;
    }

    void defend() {
        final Illusioner illusioner = (Illusioner)block.getWorld().spawnEntity(
                block.getWorld().getHighestBlockAt(block.getX(), block.getZ()).getLocation()
                        // Above the highest block
                        .add(new Vector(0.0D, 1.01D, 0.0D)),
                EntityType.ILLUSIONER);
        // In case the CustomCreatures plugin is switched off.
        illusioner.setRemoveWhenFarAway(false);

        new SpawnerHelper().set(
                block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ()),
                EntityType.SHULKER);
    }
}
