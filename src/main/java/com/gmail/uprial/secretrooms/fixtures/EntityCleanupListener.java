package com.gmail.uprial.secretrooms.fixtures;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hoglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.gmail.uprial.secretrooms.common.Formatter.format;

// Used to fix mistakes when entities have wrong params.
public class EntityCleanupListener implements Listener {
    private final CustomLogger customLogger;

    public EntityCleanupListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private int total = 0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        int fixed = 0;
        for(final Entity entity : event.getChunk().getEntities()) {
            if(entity instanceof Hoglin) {
                final Hoglin hoglin = (Hoglin)entity;
                boolean broken = false;
                if(!hoglin.getRemoveWhenFarAway()) {
                    broken = true;
                    hoglin.setRemoveWhenFarAway(true);
                }
                for(final PotionEffect effect : hoglin.getActivePotionEffects()) {
                    if(effect.getType().equals(PotionEffectType.FIRE_RESISTANCE)) {
                        broken = true;
                        hoglin.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                    }
                }
                if(broken) {
                    fixed ++;
                }
            }
        }
        if(fixed > 0) {
            total += fixed;
            customLogger.info(String.format("Fixed %d Hoglins in %s, %d in total",
                    fixed, format(event.getChunk()), total));
        }
    }
}
