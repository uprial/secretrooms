package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Nuke;
import com.gmail.uprial.railnet.populator.MagicColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class ExplosiveFireworkListener implements Listener {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    public ExplosiveFireworkListener(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFireworkExplode(FireworkExplodeEvent event) {
        if(!event.isCancelled()) {
            final Firework firework = event.getEntity();
            for(FireworkEffect effect : firework.getFireworkMeta().getEffects()) {
                for(Color color : effect.getColors()) {
                    final Integer power = MagicColor.decode(color);
                    if(power != null) {
                        if(power <= Nuke.MAX_ENGINE_POWER) {
                            firework.getWorld().createExplosion(firework.getLocation(), power, true, true);
                            if(customLogger.isDebugMode()) {
                                customLogger.debug(getMessage(firework, power));
                            }
                        } else {
                            Nuke.explode(plugin, firework.getLocation(), power, 0, () -> 2);
                            customLogger.info(getMessage(firework, power));
                        }
                    }
                }
            }
        }
    }

    private String getMessage(final Firework firework, final Integer power) {
        return String.format("Firework exploded at %s with power %d",
                format(firework.getLocation()), power);
    }
}