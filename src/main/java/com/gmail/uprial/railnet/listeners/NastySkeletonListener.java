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
    private final CustomLogger customLogger;

    public NastySkeletonListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private static final double POSITIVE_P = 0.3D;
    private static final double NEGATIVE_P = 1.0D;

    private enum E {

        USELESS(POSITIVE_P, 60, 2),
        GOOD(POSITIVE_P, 30, 1),
        AMAZING(POSITIVE_P, 5, 0),

        INCONVENIENT(NEGATIVE_P, 60, 2),
        PAINFUL(NEGATIVE_P, 30, 1),
        HARD(NEGATIVE_P, 5, 0);

        private final double probability;
        private final int duration;
        private final int amplifier;

        E(final double probability, final int duration, final int amplifier) {
            this.probability = probability;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public double getProbability() {
            return probability;
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
            .put(PotionEffectType.ABSORPTION, E.GOOD)
            .put(PotionEffectType.BLINDNESS, E.HARD) // negative
            .put(PotionEffectType.DARKNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.FIRE_RESISTANCE, E.USELESS)
            .put(PotionEffectType.GLOWING, E.USELESS)
            .put(PotionEffectType.HASTE, E.USELESS)
            .put(PotionEffectType.HEALTH_BOOST, E.AMAZING)
            .put(PotionEffectType.HERO_OF_THE_VILLAGE, E.USELESS)
            .put(PotionEffectType.HUNGER, E.PAINFUL) // negative
            .put(PotionEffectType.INVISIBILITY, E.GOOD)
            .put(PotionEffectType.JUMP_BOOST, E.GOOD)
            .put(PotionEffectType.LEVITATION, E.PAINFUL) // negative
            .put(PotionEffectType.LUCK, E.USELESS)
            .put(PotionEffectType.MINING_FATIGUE, E.INCONVENIENT) // negative
            .put(PotionEffectType.NAUSEA, E.INCONVENIENT) // negative
            .put(PotionEffectType.NIGHT_VISION, E.USELESS)
            .put(PotionEffectType.POISON, E.PAINFUL) // negative
            .put(PotionEffectType.REGENERATION, E.AMAZING)
            .put(PotionEffectType.RESISTANCE, E.GOOD)
            .put(PotionEffectType.SATURATION, E.AMAZING)
            .put(PotionEffectType.SLOW_FALLING, E.USELESS)
            .put(PotionEffectType.SLOWNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.UNLUCK, E.INCONVENIENT) // negative
            .put(PotionEffectType.WATER_BREATHING, E.USELESS)
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
                    if(Probability.PASS(entry.getValue().getProbability(), 0)) {
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
