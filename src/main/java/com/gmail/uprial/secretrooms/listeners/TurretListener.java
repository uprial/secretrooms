package com.gmail.uprial.secretrooms.listeners;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class TurretListener implements Listener {
    /*
        Implementation inspired by
        https://github.com/NerdNu/SafeCrystals/blob/master/src/nu/nerd/safecrystals/SafeCrystals.java

        The only other method called when End Crystal explodes is EntityRemoveEvent,
        but it's experimental.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof EnderCrystal) {
            final EnderCrystal crystal = (EnderCrystal) event.getEntity();

            if(TurretCron.isTurret(crystal)) {

                if ((event.getDamageSource().getDirectEntity() != null)
                        && (event.getDamageSource().getDirectEntity() instanceof Fireball)
                        // The source is expected to be removed immediately after the projectile launch
                        && (event.getDamageSource().getCausingEntity() == null)) {

                    // Prevent one turret to destroy another
                    event.setCancelled(true);

                } else {
                    // Remove heading together with its End Crystal
                    TurretCron.onDeath(crystal);
                }
            }
        }
    }
}