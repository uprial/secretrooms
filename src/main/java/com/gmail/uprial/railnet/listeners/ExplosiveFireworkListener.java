package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.firework.FireworkEngine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;

public class ExplosiveFireworkListener implements Listener {
    private final FireworkEngine fireworkEngine;

    public ExplosiveFireworkListener(FireworkEngine fireworkEngine) {
        this.fireworkEngine = fireworkEngine;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFireworkExplode(FireworkExplodeEvent event) {
        if(!event.isCancelled()) {
            fireworkEngine.onExplode(event.getEntity());
        }
    }
}