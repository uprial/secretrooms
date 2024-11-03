package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.*;

public class Populator {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    private final List<ChunkPopulator> chunkPopulators;

    public Populator(final RailNet plugin, final CustomLogger customLogger, List<ChunkPopulator> chunkPopulators) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.chunkPopulators = chunkPopulators;

        for(final World world : plugin.getServer().getWorlds()) {
            onWorldInit(world);
        }
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
                onChunkLoadOncePerServetSession(chunk);
            } finally {
                alreadyLoadedInWorld.add(chunkXZ);
            }
        }
    }

    /*
        ChunkPopulateEvent and ChunkLoadEvent.isNewChunk() are buggy,
        so I mark the checked chunks via specific secret block.

        The best specific secret block is a block unavailable even in the creative code.
        According to https://minecraft.wiki/w/Creative, I picked Barrier.
     */
    final Material secretMaterial = Material.BARRIER;

    public void onChunkLoadOncePerServetSession(final Chunk chunk) {
        final Block block = chunk.getBlock(0, chunk.getWorld().getMinHeight(), 0);
        if (!block.getType().equals(secretMaterial)) {
            try {
                populateChunk(chunk);
            } finally {
                block.setType(secretMaterial, false);
            }
        }
    }

    private void populateChunk(final Chunk chunk) {
        for(final ChunkPopulator chunkPopulator : chunkPopulators) {
            chunkPopulator.populate(chunk);
        }
    }

    public void repopulateLoaded() {
        customLogger.debug("Repopulate loaded...");
        for(final World world : plugin.getServer().getWorlds()) {
            for (final Chunk chunk : world.getLoadedChunks()) {
                populateChunk(chunk);
            }
        }
    }
}