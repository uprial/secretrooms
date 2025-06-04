package com.gmail.uprial.secretrooms.listeners;

import com.gmail.uprial.secretrooms.populator.Populator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkListener implements Listener {
    private final Populator populator;

    public ChunkListener(final Populator populator) {
        this.populator = populator;
    }

    // Our plugin has the last word on the world population.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        populator.onChunkLoad(event.getChunk());
    }
}
