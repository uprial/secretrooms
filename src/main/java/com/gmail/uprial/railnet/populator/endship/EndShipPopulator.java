package com.gmail.uprial.railnet.populator.endship;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Illusioner;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class EndShipPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;

    public EndShipPopulator(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    @Override
    public void populate(final Chunk chunk) {
        if (isAppropriate(chunk)) {
            final Block brewingStand = getBrewingStand(chunk);
            if(brewingStand != null) {
                final Illusioner illusioner = (Illusioner)brewingStand.getWorld().spawnEntity(
                        brewingStand.getLocation(),
                        EntityType.ILLUSIONER);
                // In case the CustomCreatures plugin is switched off.
                illusioner.setRemoveWhenFarAway(false);

                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s spawned", format(illusioner)));
                    customLogger.debug(String.format("EndShip[%s] populated", format(chunk)));
                }
            }
        }
    }

    /*
        According to https://minecraft.wiki/w/Brewing_Stand
        Brewing stands generate in end ships, the basement of igloos,
        and can generate in village churches.
     */
    private Block getBrewingStand(final Chunk chunk) {
        final int minY = chunk.getWorld().getMinHeight();
        final int maxY = chunk.getWorld().getMaxHeight();

        for(int y = minY; y < maxY; y++) {
            for (int x = 0; x < 16; x ++) {
                for (int z = 0; z < 16; z ++) {
                    final Block block = chunk.getBlock(x, y, z);
                    if(block.getType().equals(Material.BREWING_STAND)) {
                        return block;
                    }
                }
            }
        }

        return null;
    }

    final static String world = WorldName.END;

    private boolean isAppropriate(final Chunk chunk) {
        return (chunk.getWorld().getName().equalsIgnoreCase(world));
    }
}
