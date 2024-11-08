package com.gmail.uprial.railnet.populator.whirlpool;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.VirtualChunk;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class WhirlpoolPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;
    //private final RailNet plugin;

    VirtualChunk vc;

    public WhirlpoolPopulator(final RailNet plugin, final CustomLogger customLogger) {
        //this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @Override
    public void populate(final Chunk chunk) {
        if(isAppropriate(chunk)) {
            vc = new VirtualChunk("Whirlpool", chunk, BlockFace.NORTH);

            int minX = 1;
            int minY = vc.getSeaLevel();
            int minZ = 1;
            for(int x = 1; x < 15; x++) {
                for(int z = 1; z < 15; z++) {
                    int y = vc.getSeaLevel();
                    while((y > vc.getMinHeight()) && (isWaterLayer(x, y, z))) {
                        y--;
                    }
                    if(y < minY) {
                        minX = x;
                        minY = y;
                        minZ = z;
                    }
                }
            }

            if(minY >= vc.getSeaLevel()) {
                if(customLogger.isDebugMode()) {
                    // No water
                    customLogger.debug(String.format("Whirlpool %s:%d:%d can't be populated",
                            chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                }
                return;
            }

            // 10 blocks above the bottom
            for(int dx = -1; dx <= 1; dx++) {
                for(int dz = -1; dz <= 1; dz++) {
                    int y = minY;
                    while((y > vc.getMinHeight()) && isWater(minX + dx, y,  minZ + dz)) {
                        y--;
                    }

                    if(vc.get(minX + dx, y, minZ + dz).getType().equals(Material.MAGMA_BLOCK)) {
                        if(customLogger.isDebugMode()) {
                            // Idempotency marker
                            customLogger.debug(String.format("Whirlpool %s:%d:%d is already populated",
                                    chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
                        }
                        return;
                    }

                    vc.applyPhysicsOnce();
                    vc.set(minX + dx, y, minZ + dz, Material.MAGMA_BLOCK);
                }
            }

            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("Whirlpool %s:%d:%d populated",
                        chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
            }
        }
    }

    private boolean isWater(final int x, final int y, final int z) {
        return vc.get(x, y, z).getType().equals(Material.WATER);
    }

    private boolean isWaterLayer(final int x, final int y, final int z) {
        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                if(!isWater(x + dx, y, z + dz)) {
                    return false;
                }
            }
        }
        return true;
    };

    final static String world = "world";
    final static int xRate = 4;
    final static int zRate = 3;

    private boolean isAppropriate(final Chunk chunk) {
        return (chunk.getWorld().getName().equalsIgnoreCase(world))
                && (Math.abs(chunk.getWorld().getSeed()) % xRate == Math.abs(chunk.getX()) % xRate)
                && (Math.abs(chunk.getWorld().getSeed()) % zRate == Math.abs(chunk.getZ()) % zRate);
    }
}