package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class Populator {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    private final List<ChunkPopulator> chunkPopulators;

    private final ChunkQueue queue;

    public Populator(final RailNet plugin, final CustomLogger customLogger, List<ChunkPopulator> chunkPopulators) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.chunkPopulators = chunkPopulators;

        for(final World world : plugin.getServer().getWorlds()) {
            onWorldInit(world);
        }

        queue = new ChunkQueue(plugin);
    }

    public void stop() {
        queue.cancel();
    }
    /*
        Plugins are enabled on server start.

        But some chunks are already generated
        before the first initiation of any plugin
        unless the plugin is configured in bukkit.yml as a generator.

        The following potential functions are buggy:
            world.getChunkAt(x, y, generate = false)
            world.isChunkGenerated(x, y)
            etc.

        So I check the chunks loaded on server start.
     */
    private void onWorldInit(final World world) {
        for(final Chunk chunk : world.getLoadedChunks()) {
            onChunkLoad(chunk);
        }
    }

    // Simply prevent cycling
    private final Map<String, Set<ChunkXZ>> alreadyLoaded = new HashMap<>();

    public void onChunkLoad(final Chunk chunk) {
        final Set<ChunkXZ> alreadyLoadedInWorld
                = alreadyLoaded.computeIfAbsent(chunk.getWorld().getName(), k -> new HashSet<>());

        final ChunkXZ chunkXZ = new ChunkXZ(chunk.getX(), chunk.getZ());
        if(!alreadyLoadedInWorld.contains(chunkXZ)) {
            try {
                onChunkLoadOncePerServerSession(chunk);
            } finally {
                alreadyLoadedInWorld.add(chunkXZ);
            }
        }
    }

    /*
        ChunkPopulateEvent and ChunkLoadEvent.isNewChunk() are buggy,
        so I mark the checked chunks via specific secret block.

        The best specific secret block is a block unavailable even in the creative code.
        According to https://minecraft.wiki/w/Creative, I picked Barrier and Light
     */
    final Map<Material,Material> idempotencyMap = ImmutableMap.<Material,Material>builder()
            .put(Material.BEDROCK, Material.BARRIER)
            .put(Material.AIR, Material.LIGHT)
            .build();

    final Set<Material> finalMaterials = ImmutableSet.<Material>builder()
            .addAll(idempotencyMap.values())
            .build();

    public void onChunkLoadOncePerServerSession(final Chunk chunk) {
        final Block block = chunk.getBlock(0, chunk.getWorld().getMinHeight(), 0);
        if (!finalMaterials.contains(block.getType())) {
            try {
                populateChunk(chunk);
            } finally {
                block.setType(idempotencyMap.get(block.getType()), false);
            }
        }
    }

    private void populateChunk(final Chunk chunk) {
        for(final ChunkPopulator chunkPopulator : chunkPopulators) {
            chunkPopulator.populate(chunk);
        }
    }

    public int repopulateLoaded(final String worldName, final int x, final int z, final int radius) {
        customLogger.debug(
                String.format("Repopulate loaded chunks in %s:%d:%d with radius %d...",
                        worldName, x, z, radius));

        int counter = 0;
        for(final World world : plugin.getServer().getWorlds()) {
            if(world.getName().equalsIgnoreCase(worldName)) {
                for (final Chunk chunk : world.getLoadedChunks()) {
                    if ((Math.abs(chunk.getX() - x) < radius)
                            && (Math.abs(chunk.getZ() - z) < radius)) {
                        populateChunk(chunk);
                        counter++;
                    }
                }
            }
        }

        return counter;
    }
}