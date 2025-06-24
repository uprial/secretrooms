package com.gmail.uprial.secretrooms.listeners;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TurretListener implements Listener {
    final private TurretCron turretCron;

    public TurretListener(final TurretCron turretCron) {
        this.turretCron = turretCron;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent event) {
        if((!event.isCancelled()) && (event.getEntity() instanceof EnderCrystal)) {
            turretCron.onExplode((EnderCrystal)event.getEntity());
        }
    }
}