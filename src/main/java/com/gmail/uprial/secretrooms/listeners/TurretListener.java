package com.gmail.uprial.secretrooms.listeners;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class TurretListener implements Listener {
    final private TurretCron turretCron;

    public TurretListener(final TurretCron turretCron) {
        this.turretCron = turretCron;
    }
    /*
        Implementation inspired by
        https://github.com/NerdNu/SafeCrystals/blob/master/src/nu/nerd/safecrystals/SafeCrystals.java

        The only other method called when End Crystal explodes is EntityRemoveEvent,
        but it's experimental.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof EnderCrystal) {
            turretCron.onDeath((EnderCrystal)event.getEntity());
        }
    }
}