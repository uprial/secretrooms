package com.gmail.uprial.secretrooms;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SecretRoomsExecutor {
    private final SecretRooms plugin;
    SecretRoomsExecutor(final SecretRooms plugin) {
        this.plugin = plugin;
    }

    int breakTerrain(final String worldName, final int x, final int y, final int z, final int radius) {
        final World world = plugin.getServer().getWorld(worldName);
        int counter = 0;
        if(world != null) {
            final ItemStack tool = new ItemStack(Material.NETHERITE_PICKAXE);
            //tool.addEnchantment(Enchantment.FORTUNE, 3);
            /*
                Since we're breaking blocks
                it's better to s tart from top layers that may affect lower levels.
             */
            for(int dy = radius; dy >= -radius; dy--) {
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        if(world.getBlockAt(x + dx, y + dy, z + dz).breakNaturally(tool)) {
                            counter++;
                        }
                    }
                }
            }
        }

        return counter;
    }

    Map<String,Integer> getLoadedStats(final Material material) {
        final Map<String,Integer> stats = new HashMap<>();
        for(final World world : plugin.getServer().getWorlds()) {
            for (final Chunk chunk : world.getLoadedChunks()) {
                final int minY = chunk.getWorld().getMinHeight();
                final int maxY = chunk.getWorld().getMaxHeight();
                for(int y = minY; y < maxY; y++) {
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            final Block block = chunk.getBlock(x, y, z);
                            if(block.getType().equals(material)) {
                                stats.merge(world.getName(), 1, Integer::sum);
                            }
                        }
                    }
                }
            }
        }

        return stats;
    }

    List<Block> getLoadedBlocks(final Material material) {
        final List<Block> blocks = new ArrayList<>();
        for(final World world : plugin.getServer().getWorlds()) {
            for (final Chunk chunk : world.getLoadedChunks()) {
                final int minY = chunk.getWorld().getMinHeight();
                final int maxY = chunk.getWorld().getMaxHeight();
                for(int y = minY; y < maxY; y++) {
                    for(int x = 0; x < 16; x++) {
                        for(int z = 0; z < 16; z++) {
                            final Block block = chunk.getBlock(x, y, z);
                            if(block.getType().equals(material)) {
                                blocks.add(block);
                            }
                        }
                    }
                }
            }
        }

        return blocks;
    }
}
