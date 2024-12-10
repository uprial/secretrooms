package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.google.common.collect.ImmutableMap;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Map;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.Utils.seconds2ticks;

public class NastySkeletonListener implements Listener {
    private static final double PROBABILITY = 5.0D;

    private final CustomLogger customLogger;

    public NastySkeletonListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private enum E {

        INCONVENIENT(300, 2),
        PAINFUL(60, 1),
        HARD(5, 0);

        private final int duration;
        private final int amplifier;

        E(final int duration, final int amplifier) {
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public int getDuration() {
            return duration;
        }
    }
    /*
        Effect type options: effect -> E

        Source: MineshaftPopulator.chestLootTable.POTION
     */
    final private Map<PotionEffectType,E> effectMap = ImmutableMap .<PotionEffectType, E>builder()
            .put(PotionEffectType.BLINDNESS, E.HARD) // negative
            .put(PotionEffectType.DARKNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.HUNGER, E.PAINFUL) // negative
            .put(PotionEffectType.LEVITATION, E.PAINFUL) // negative
            .put(PotionEffectType.MINING_FATIGUE, E.INCONVENIENT) // negative
            .put(PotionEffectType.NAUSEA, E.INCONVENIENT) // negative
            .put(PotionEffectType.POISON, E.PAINFUL) // negative
            .put(PotionEffectType.SLOWNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.UNLUCK, E.INCONVENIENT) // negative
            .put(PotionEffectType.WEAKNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.WITHER, E.PAINFUL) // negative
            .build();

    @SuppressWarnings({"unused", "MethodMayBeStatic"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource shooter = projectile.getShooter();
            if ((projectile instanceof Arrow) && (shooter instanceof AbstractSkeleton)) {
                final Arrow arrow = (Arrow)projectile;
                for (Map.Entry<PotionEffectType, E> entry : effectMap.entrySet()) {
                    if(Probability.PASS(PROBABILITY, 0)) {
                        arrow.addCustomEffect(
                                new PotionEffect(entry.getKey(),
                                        seconds2ticks(entry.getValue().getDuration()),
                                        entry.getValue().getAmplifier()),
                                true);

                        if(customLogger.isDebugMode()) {
                            customLogger.info(String.format("Arrow of %s got %s",
                                    format((AbstractSkeleton)shooter), entry.getKey()));
                        }
                    }
                }
            }
        }
    }
}
