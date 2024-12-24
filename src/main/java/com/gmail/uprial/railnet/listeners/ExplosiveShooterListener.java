package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.MetadataHelper.*;

public class ExplosiveShooterListener implements Listener {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    public ExplosiveShooterListener(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    private static final String MK_EXPLOSION = "rn_explosion";
    private static final double EXPLOSION_PROBABILITY = 1.0D;
    private static final float EXPLOSION_POWER = 2.0f;

    @SuppressWarnings({"unused", "MethodMayBeStatic"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource shooter = projectile.getShooter();
            if ((projectile instanceof Arrow || projectile instanceof ThrownPotion)
                    && (shooter instanceof LivingEntity)
                    && !(shooter instanceof Player)) {

                final LivingEntity entity = (LivingEntity)shooter;

                final Boolean explosion = getMetadataOrDefault(plugin, entity, MK_EXPLOSION, ()  -> {
                    final boolean newExplosion;
                    if(Probability.PASS(EXPLOSION_PROBABILITY, 0)) {
                        newExplosion = true;
                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s of %s got explosion with power %.1f",
                                    projectile.getType(), format(entity), EXPLOSION_POWER));
                        }
                    } else {
                        newExplosion = false;
                    }

                    return newExplosion;
                });

                if((explosion) && (distance(projectile, entity) > EXPLOSION_POWER)) {
                    projectile.getWorld().createExplosion(projectile.getLocation(), EXPLOSION_POWER, true);
                }
            }
        }
    }

    private double distance(final Entity entity1, final Entity entity2) {
        return entity1.getLocation().distance(entity2.getLocation());
    }
}
