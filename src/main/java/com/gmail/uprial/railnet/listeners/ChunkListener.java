package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.generator.Generator;
import com.gmail.uprial.railnet.map.ChunkXZ;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkListener implements Listener {
    private final Generator generator;

    // Prevent cycling
    private final Map<String,Set<ChunkXZ>> alreadyLoaded = new HashMap<>();

    public ChunkListener(final Generator generator) {
        this.generator = generator;
    }

    // Our plugin has the last word on the world generation.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        final Set<ChunkXZ> alreadyLoadedInWorld
                = alreadyLoaded.computeIfAbsent(event.getWorld().getName(), k -> new HashSet<>());

        final ChunkXZ chunkXZ = new ChunkXZ(event.getChunk().getX(), event.getChunk().getZ());
        if(!alreadyLoadedInWorld.contains(chunkXZ)) {
            generator.onChunkLoad(event.getChunk());
            alreadyLoadedInWorld.add(chunkXZ);
        }
    }
}
