package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.google.common.collect.ImmutableMap;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.MetadataHelper.getMetadata;
import static com.gmail.uprial.railnet.common.MetadataHelper.setMetadata;
import static com.gmail.uprial.railnet.common.Utils.seconds2ticks;

public class NastySkeletonListener implements Listener {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    public NastySkeletonListener(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    private static final String MK_EFFECTS = "rn_effects";
    private static final double POSITIVE_PROBABILITY = 0.3D;
    private static final double NEGATIVE_PROBABILITY = 1.0D;

    private static final String MK_EXPLOSION = "rn_explosion";
    private static final double EXPLOSION_PROBABILITY = 1.0D;
    private static final float EXPLOSION_POWER = 2.0f;

    private enum E {

        USELESS(POSITIVE_PROBABILITY, 30, 2),
        GOOD(POSITIVE_PROBABILITY, 15, 1),
        AMAZING(POSITIVE_PROBABILITY, 5, 0),

        INCONVENIENT(NEGATIVE_PROBABILITY, 60, 2),
        PAINFUL(NEGATIVE_PROBABILITY, 30, 1),
        HARD(NEGATIVE_PROBABILITY, 10, 0);

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
            // BAD_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.BLINDNESS, E.HARD) // negative
            // CONDUIT_POWER: duplicates WATER_BREATHING
            .put(PotionEffectType.DARKNESS, E.INCONVENIENT) // negative
            // DOLPHINS_GRACE: doesn't work
            .put(PotionEffectType.FIRE_RESISTANCE, E.USELESS)
            .put(PotionEffectType.GLOWING, E.USELESS)
            .put(PotionEffectType.HASTE, E.USELESS)
            .put(PotionEffectType.HEALTH_BOOST, E.AMAZING)
            .put(PotionEffectType.HERO_OF_THE_VILLAGE, E.USELESS)
            .put(PotionEffectType.HUNGER, E.PAINFUL) // negative
            // INFESTED: doesn't work
            .put(PotionEffectType.INSTANT_DAMAGE, E.HARD) // negative
            .put(PotionEffectType.INSTANT_HEALTH, E.GOOD)
            .put(PotionEffectType.INVISIBILITY, E.GOOD)
            .put(PotionEffectType.JUMP_BOOST, E.GOOD)
            .put(PotionEffectType.LEVITATION, E.PAINFUL) // negative
            .put(PotionEffectType.LUCK, E.USELESS)
            .put(PotionEffectType.MINING_FATIGUE, E.INCONVENIENT) // negative
            .put(PotionEffectType.NAUSEA, E.INCONVENIENT) // negative
            .put(PotionEffectType.NIGHT_VISION, E.USELESS)
            // OOZING: has no lasting effect
            .put(PotionEffectType.POISON, E.PAINFUL) // negative
            // RAID_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.REGENERATION, E.AMAZING)
            .put(PotionEffectType.RESISTANCE, E.GOOD)
            .put(PotionEffectType.SATURATION, E.AMAZING)
            .put(PotionEffectType.SLOW_FALLING, E.USELESS)
            .put(PotionEffectType.SLOWNESS, E.INCONVENIENT) // negative
            // TRIAL_OMEN: duplicates OMINOUS_BOTTLE
            .put(PotionEffectType.UNLUCK, E.INCONVENIENT) // negative
            .put(PotionEffectType.WATER_BREATHING, E.USELESS)
            .put(PotionEffectType.WEAKNESS, E.INCONVENIENT) // negative
            // WEAVING: has no lasting effect
            // WIND_CHARGED: has no lasting effect
            .put(PotionEffectType.WITHER, E.PAINFUL) // negative
            .build();

    @SuppressWarnings({"unused", "MethodMayBeStatic"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource shooter = projectile.getShooter();
            if ((projectile instanceof Arrow) && (shooter instanceof AbstractSkeleton)) {
                final AbstractSkeleton skeleton = (AbstractSkeleton)shooter;

                Set<PotionEffect> potionEffects = getMetadata(skeleton, MK_EFFECTS);
                if(potionEffects == null) {
                    potionEffects = new HashSet<>();
                    for (Map.Entry<PotionEffectType, E> entry : effectMap.entrySet()) {
                        if (Probability.PASS(entry.getValue().getProbability(), 0)) {
                            potionEffects.add(
                                    new PotionEffect(entry.getKey(),
                                            seconds2ticks(entry.getValue().getDuration()),
                                            entry.getValue().getAmplifier()));

                            if(customLogger.isDebugMode()) {
                                customLogger.info(String.format("Arrow of %s got %s",
                                        format(skeleton), entry.getKey()));
                            }
                        }
                    }

                    setMetadata(plugin, skeleton, MK_EFFECTS, potionEffects);
                }

                for(final PotionEffect potionEffect : potionEffects) {
                    ((Arrow)projectile).addCustomEffect(potionEffect, true);
                }
            }
        }
    }

    @SuppressWarnings({"unused", "MethodMayBeStatic"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource shooter = projectile.getShooter();
            if ((projectile instanceof Arrow) && (shooter instanceof AbstractSkeleton)) {
                final AbstractSkeleton skeleton = (AbstractSkeleton)shooter;

                Boolean explosion = getMetadata(skeleton, MK_EXPLOSION);
                if(explosion == null) {
                    explosion = false;
                    if(Probability.PASS(EXPLOSION_PROBABILITY, 0)) {
                        explosion = true;
                        if (customLogger.isDebugMode()) {
                            customLogger.info(String.format("Arrow of %s got explosion with power %.1f",
                                    format(skeleton), EXPLOSION_POWER));
                        }
                    }

                    setMetadata(plugin, skeleton, MK_EXPLOSION, explosion);
                }

                if(explosion) {
                    projectile.getWorld().createExplosion(projectile.getLocation(), EXPLOSION_POWER, true);
                }
            }
        }
    }
}
