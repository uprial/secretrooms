package com.gmail.uprial.railnet.listeners;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

import java.util.Set;

public class StrongBlockListener implements Listener {
    private final static Set<Material> strongBlocks = ImmutableSet.<Material>builder()
            .add(Material.LIME_STAINED_GLASS)
            .add(Material.LIME_STAINED_GLASS_PANE)
            .build();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockExplode(BlockExplodeEvent event) {
        if(!event.isCancelled()) {
            event.blockList().removeIf(block -> strongBlocks.contains(block.getType()));
        }
    }
}
