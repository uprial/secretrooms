package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.AimHelper;
import com.gmail.uprial.railnet.common.RandomUtils;
import com.gmail.uprial.railnet.common.WorldName;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.gmail.uprial.railnet.common.Formatter.format;
import static com.gmail.uprial.railnet.common.Utils.seconds2ticks;
import static java.lang.Math.*;

public class NastyEnderDragonListener implements Listener {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    private final Set<Location> bedrocks = new HashSet<>();

    public NastyEnderDragonListener(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (isAppropriateWorld(event.getChunk().getWorld())) {
            updateBedrocksCache(event.getChunk().getWorld());
        }
    }

    /*
        According to https://minecraft.wiki/w/End_spike
        10 pillars generate in a 43-block radius circle around the exit portal,
        which is up to ceil(43 / 16) = 3 chunks.

        (34^2 + 26^2)^0.5 = 42.8 <= 43
        in chunks it's
        (3^2 + 2^2)^0.5 = 3.6 <= 4

        We also need to exclude the central bedrock above the end portal.

     */
    private static final int    PILLARS_COUNT    = 10;
    private static final double MIN_CHUNK_RADIUS = 0.5D;
    private static final double MAX_CHUNK_RADIUS = 4;
    private static final int    MAX_CHUNK_XZ     = 3;

    private final AtomicBoolean bedrocksCacheUpdated = new AtomicBoolean(false);
    private void updateBedrocksCache(final World world) {
        // update the cache only once
        if(!bedrocksCacheUpdated.get()) {
            bedrocksCacheUpdated.set(true);

            for (int x = -MAX_CHUNK_XZ; x <= MAX_CHUNK_XZ; x++) {
                for (int z = -MAX_CHUNK_XZ; z <= MAX_CHUNK_XZ; z++) {
                    final double distance = getDistance(x, z);
                    if ((distance > MIN_CHUNK_RADIUS) && (distance <= MAX_CHUNK_RADIUS)) {
                        final Location bedrock = searchBedrock(world.getChunkAt(x, z, true));
                        if(bedrock != null) {
                            bedrocks.add(bedrock);

                            if (customLogger.isDebugMode()) {
                                customLogger.debug(String.format("Detected a bedrock at %s", format(bedrock)));
                            }
                        }
                    }
                }
            }
            if(bedrocks.size() != PILLARS_COUNT) {
                customLogger.error(String.format("Detected %d bedrocks instead of %d",
                        bedrocks.size(), PILLARS_COUNT));
            }
        }
    }

    private Location searchBedrock(final Chunk chunk) {
        /*
            According to https://minecraft.wiki/w/End_spike
            pillars generate between 76 and 103 Y.
         */
        final int minY = max(70, chunk.getWorld().getMinHeight());
        final int maxY = min(110, chunk.getWorld().getMaxHeight());
        // Takes 3-10ms per chunk
        for (int y = minY; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    final Block block = chunk.getBlock(x, y, z);
                    if (block.getType().equals(Material.BEDROCK)) {
                        return block.getLocation();
                    }
                }
            }
        }

        return null;
    }

    private static final int RESURRECTION_INTERVAL = 60_000; // 1 minute
    private long lastResurrection = 0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!event.isCancelled()
                && (event.getEntity() instanceof EnderDragon)
                && (isAppropriateWorld(event.getEntity().getWorld()))) {

            final long currentTime = System.currentTimeMillis();
            if(currentTime - lastResurrection < RESURRECTION_INTERVAL) {
                // Ender Dragon was attacked, but the resurrection interval hasn't passed
                return;
            }
            lastResurrection = currentTime;

            final World world = event.getEntity().getWorld();

            final Collection<EnderCrystal> crystals = world.getEntitiesByClass(EnderCrystal.class);

            final Set<Location> bedrocksWithoutCrystals = new HashSet<>();
            for(final Location bedrock : bedrocks) {
                boolean found = false;
                for (final EnderCrystal crystal : crystals) {
                    if (crystal.getLocation().distance(bedrock2crystal(bedrock)) < 1.0D) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    bedrocksWithoutCrystals.add(bedrock);
                }
            }

            if(!bedrocksWithoutCrystals.isEmpty()) {
                /*
                    Don't let the player predict
                    which crystal location without crystals will be resurrected.
                 */
                final Location bedrock = RandomUtils.getSetItem(bedrocksWithoutCrystals);

                resurrect(world, bedrock);

                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("Crystal at %s resurrected", format(bedrock)));
                }
            } else {
                if (customLogger.isDebugMode()) {
                    customLogger.debug("Ender Dragon attacked, but all crystals are in place");
                }
            }

            final Entity damager = getRealSource(event.getDamager());
            if(damager instanceof Player) {
                launch((EnderDragon)event.getEntity(), (Player)damager);
            }
        }
    }

    /*
        According to https://minecraft.wiki/w/End_spike,
        any blocks the player had placed within 10 blocks
        in all directions of the bedrock block at the top of the end spikes are deleted.
     */
    private static final int CLEARANCE_RADIUS = 10;
    private void resurrect(final World world, final Location bedrock) {
        final int x = bedrock.getBlockX();
        final int y = bedrock.getBlockY();
        final int z = bedrock.getBlockZ();
        world.createExplosion(bedrock2crystal(bedrock), CLEARANCE_RADIUS);

        for (int dy = -CLEARANCE_RADIUS; dy < CLEARANCE_RADIUS; dy++) {
            for (int dx = -CLEARANCE_RADIUS; dx < CLEARANCE_RADIUS; dx++) {
                for (int dz = -CLEARANCE_RADIUS; dz < CLEARANCE_RADIUS; dz++) {
                    final Block block = world.getBlockAt(x + dx, y + dy, z + dz);

                    final Material material;
                    if((dx == 0) && (dy == 0) && (dz == 0)) {
                        material = Material.BEDROCK;
                    } else if ((dy < 0) && (getDistance(dx, dz) <= 3.3D)) {
                        material = Material.OBSIDIAN;
                    } else if ((dy < 0) && (!block.getType().equals(Material.OBSIDIAN))) {
                        material = Material.AIR;
                    } else if ((dy == 3) && (abs(dx) <= 3) && (abs(dz) <= 3)) {
                        material = Material.IRON_BARS;
                    } else if ((dy >= 0) && (dy < 3) &&
                            (abs(dx) == 3 && abs(dz) <= 3 || abs(dx) <= 3 && abs(dz) == 3)) {
                        material = Material.IRON_BARS;
                    } else {
                        material = Material.AIR;
                    }

                    if(!block.getType().equals(material)) {
                        block.setType(material);
                    }
                }
            }
        }

        world.spawnEntity(bedrock2crystal(bedrock), EntityType.END_CRYSTAL);
    }

    private static final int BALLS_COUNT    = 10;
    private static final int BALLS_INTERVAL = 3;
    private void launch(final EnderDragon enderDragon, final Player player) {
        for(int i = 0; i < BALLS_COUNT; i++) {
            plugin.scheduleDelayed(() -> {
                if(!enderDragon.isValid() || !player.isValid()) {
                    return;
                }

                AimHelper.setTarget(enderDragon, player);

                final DragonFireball dragonFireball
                        = enderDragon.launchProjectile(DragonFireball.class);

                final Vector direction
                        = AimHelper.getDirection(enderDragon.getLocation(), player.getEyeLocation());

                direction.multiply(0.1D);
                dragonFireball.setVelocity(direction);

                // Fixture for TakeAim
                Bukkit.getPluginManager().callEvent(
                        new ProjectileLaunchEvent(dragonFireball));

            }, seconds2ticks(BALLS_INTERVAL * i));
        }
    }

    private static Location bedrock2crystal(final Location bedrockLocation) {
        // Center of the block above
        return bedrockLocation.clone().add(0.5D, 1.0D, 0.5D);
    }

    private static boolean isAppropriateWorld(final World world) {
        return world.getName().equalsIgnoreCase(WorldName.END);
    }

    private static Entity getRealSource(final Entity source) {
        if (source instanceof Projectile) {
            final Projectile projectile = (Projectile)source;
            final ProjectileSource projectileSource = projectile.getShooter();
            if (projectileSource instanceof Entity) {
                return (Entity)projectileSource;
            }
        }
        return source;
    }

    private static double getDistance(final int x, final int z) {
        return Math.sqrt(x * x + z * z);
    }
}