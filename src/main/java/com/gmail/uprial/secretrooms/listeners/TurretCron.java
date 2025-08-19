package com.gmail.uprial.secretrooms.listeners;

import com.gmail.uprial.secretrooms.SecretRooms;
import com.gmail.uprial.secretrooms.common.AngerHelper;
import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static com.gmail.uprial.secretrooms.common.Formatter.format;
import static com.gmail.uprial.secretrooms.common.Utils.seconds2ticks;

public class TurretCron extends BukkitRunnable {
    public enum TurretType {
        SMALL,
        BIG
    }

    /*
        According to https://minecraft.wiki/w/End_Crystal,
        the weight and height of the End Crystals are 2.0.
     */
    private static final double TURRET_BODY_SIZE = 2.0D;

    private static class Turret {

        private final float explosionPower;
        private final float maxBlastResistance;
        private final double safeDistance;

        Turret(final float explosionPower,
               final float maxBlastResistance,
               final double entityRange,
               final double blockRange) {
            this.explosionPower = explosionPower;
            this.maxBlastResistance = maxBlastResistance;
            this.safeDistance = TURRET_BODY_SIZE + Math.max(entityRange, blockRange);
        }

        float getExplosionPower() {
            return explosionPower;
        }

        float getMaxBlastResistance() {
            return maxBlastResistance;
        }

        double getSafeDistance() {
            return safeDistance;
        }
    }

    private static final Map<Material,Turret> TURRETS = ImmutableMap.<Material,Turret>builder()
            /*
                According to https://minecraft.wiki/w/Explosion#Causes,

                CAUSE    | POWER | MAX. BLAST RESISTANCE | MAX. RANGE
                Fireball | 1     | 3                     | 2.0 - 1.5
                Creeper  | 3     | 9                     | 6.0 - 5.1
                Wither   | 8     | 9                     | 16.0 - 13.8

                And the Heavy Core blast resistance is 30.
             */
            .put(Material.DRAGON_HEAD,
                    new Turret(1.0f, 3.0f, 2.0D, 1.5D))
            .put(Material.HEAVY_CORE,
                    new Turret(3.0f, 9.0f, 6.0D, 5.1D))
            .build();

    private static final Map<TurretType,Material> TURRET_TYPES = ImmutableMap.<TurretType,Material>builder()
            .put(TurretType.BIG, Material.HEAVY_CORE)
            .put(TurretType.SMALL, Material.DRAGON_HEAD)
            .build();

    private static final int SHOOT_INTERVAL = seconds2ticks(3);

    /*
        Don't shoot from the inside of the crystal, give a space for the defence buildings.

        The End Crystals move vertically, taking two blocks.

        If the blocks are bordered with 4 defence blocks in X and Z coordinates,
        one border below and one border above,
        the distance between getEnderCrystalCenter() and these 4*2+2=10 defence blocks
        might be up to: (2.0 ^ 2 + 0.5 ^ 2 * 2) ^ 0.5 = 2.12.
     */
    private static final double DEFENCE_DISTANCE = 3.0D;

    private final SecretRooms plugin;
    private final CustomLogger customLogger;
    private final int timeoutInMs;

    public TurretCron(final SecretRooms plugin,
                      final CustomLogger customLogger,
                      final int timeoutInMs) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.timeoutInMs = timeoutInMs;

        runTaskTimer(plugin, SHOOT_INTERVAL, SHOOT_INTERVAL);
    }

    public static void spawn(final VirtualChunk vc, final int x, final int y, final int z, final TurretType type) {
        vc.set(x, y - 1, z, Material.OBSIDIAN);
        vc.set(x, y + 1, z, TURRET_TYPES.get(type));

        final Block crystal = vc.get(x, y, z);

        vc.getWorld().spawnEntity(
                new Location(vc.getWorld(),
                        0.5D + crystal.getX(),
                        0.5D + crystal.getY(),
                        0.5D + crystal.getZ()),
                EntityType.END_CRYSTAL);

        // Defence buildings
        for(int dy = 0; dy < 2; dy++) {
            for (int i = -1; i <= +1; i += 2) {
                vc.set(x + i, y + dy, z, Material.BLACK_STAINED_GLASS);
                vc.set(x, y + dy, z + i, Material.BLACK_STAINED_GLASS);
            }
        }
        vc.set(x, y + 2, z, Material.BLACK_STAINED_GLASS);
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public void run() {
        final long start = System.currentTimeMillis();
        trigger();
        final long end = System.currentTimeMillis();
        if(end - start >= timeoutInMs) {
            customLogger.warning(String.format("Turret cron took %dms", end - start));
        }
    }

    private void trigger() {

        final Map<UUID, List<Player>> worldsPlayers = new HashMap<>();
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if(AngerHelper.isValidPlayer(player)) {
                worldsPlayers
                        .computeIfAbsent(player.getWorld().getUID(), (k) -> new ArrayList<>())
                        .add(player);
            }
        }
        if(worldsPlayers.isEmpty()) {
            return;
        }

        for(final World world : plugin.getServer().getWorlds()) {
            if(worldsPlayers.containsKey(world.getUID())) {
                for(final EnderCrystal crystal : world.getEntitiesByClass(EnderCrystal.class)) {
                    if (!crystal.isValid()) {
                        continue;
                    }
                    final Turret turret = getTurret(crystal);
                    if(turret != null) {
                        final Player player = getClosestVisiblePlayer(crystal, turret, worldsPlayers.get(world.getUID()));
                        if(player != null) {
                            launch(crystal, player, (final Fireball fireball) -> {
                                fireball.setYield(turret.getExplosionPower());

                                if (customLogger.isDebugMode()) {
                                    customLogger.debug(String.format("%s launched a %s x %.2f at %s",
                                            format(crystal), fireball.getType(), fireball.getYield(), format(player)));
                                }
                            });
                        }
                    }
                }
            }
        }
    }

    void onDeath(final EnderCrystal crystal) {
        if (getTurret(crystal) != null) {
            // Break Dragon Head or Heavy Core together with its End Crystal
            getHeading(crystal).setType(Material.AIR);
        }
    }

    private static Turret getTurret(final EnderCrystal crystal) {
        final Block heading = getHeading(crystal);

        return TURRETS.get(heading.getType());
    }

    private static Block getHeading(final EnderCrystal crystal) {
        final Location location = crystal.getLocation().clone().add(0.0D, 1.0D, 0.0D);
        // I tested, and getMaxHeight() already can't be set.
        if (location.getBlockY() < location.getWorld().getMaxHeight()) {
            return location.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }

    private static Player getClosestVisiblePlayer(final EnderCrystal crystal, final Turret turret, final List<Player> players) {
        return AngerHelper.getSmallestItem(players, (final Player player) -> {
            // AngerHelper.isValidPlayer(player) is already checked in run()
            if(isSeeingPlayer(crystal, turret, player)) {
                return TakeAimAdapter.getAimPoint(player).distance(crystal.getLocation());
            } else {
                return null;
            }
        });
    }

    private static boolean isSeeingPlayer(final EnderCrystal crystal, final Turret turret, final Player player) {
        // Don't shoot across the whole map.
        if(!AngerHelper.isSimulated(crystal, player)) {
            return false;
        }

        final Location toLocation = TakeAimAdapter.getAimPoint(player);

        // Make sure the launch point isn't too close to the potential target
        final Location center = getEnderCrystalCenter(crystal);
        if(toLocation.distance(center) < turret.getSafeDistance()) {
            return false;
        }

        final Location launchPoint = getLaunchPoint(crystal, player);
        // Check for direct vision
        final RayTraceResult rayTraceResult = AngerHelper.rayTraceBlocks(
                launchPoint,
                toLocation,
                // Fireballs don't care about fluids
                FluidCollisionMode.NEVER);

        // Has no blocks between
        if((rayTraceResult == null) || (rayTraceResult.getHitBlock() == null)) {
            return true;
        }

        // Can break a block between
        final Block hitBlock = rayTraceResult.getHitBlock();
        if(!isBreakable(hitBlock, turret)) {
            return false;
        }

        // Make sure the launch point isn't too close to the potential hit block
        if(hitBlock.getLocation().distance(center) < turret.getSafeDistance()) {
            return false;
        }

        final Block blockBefore = getDirectedBlock(
                hitBlock,
                AngerHelper.getDirection(toLocation, launchPoint));

        // Can break a block before the block between
        return isBreakable(blockBefore, turret);
    }

    private static void launch(final EnderCrystal crystal,
                        final Player player,
                        final TakeAimAdapter.LaunchFireballCallback<Fireball> callback) {

        final Location fromLocation = getLaunchPoint(crystal, player);
        /*
            According to https://minecraft.wiki/w/Bat,
            Height: 0.9 blocks
            Width: 0.5 blocks

            Based on logs, the Bat location differs from the launched projectile location
            for 72.04-71.59=0.45 in Y-position:

                Bat: world_the_end:-11.05:71.59:-144.54
                Fireball: world_the_end:-11.05:72.04:-144.54
         */
        fromLocation.subtract(0.0D, 0.45, 0.0D);

        /*
            According to https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Ambient.html,
            Bat is the only ambient mob.
         */
        final Mob mob = (Mob)crystal.getWorld().spawnEntity(fromLocation, EntityType.BAT);

        /*
            To avoid any collision,
            even considering the mob will be immediately removed in finally().
         */
        mob.getAttribute(Attribute.SCALE).setBaseValue(0.01D);

        try {
            TakeAimAdapter.launchFireball(mob, player,
                    EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
                    Fireball.class, callback);
        } finally {
            mob.remove();
        }
    }

    private static Location getEnderCrystalCenter(final EnderCrystal crystal) {
        return crystal.getLocation().clone()
                .add(0.5D, TURRET_BODY_SIZE / 2.0D, 0.5D);
    }

    private static Location getLaunchPoint(final EnderCrystal crystal, final Player player) {
        final Location center = getEnderCrystalCenter(crystal);

        final Vector direction = AngerHelper.getDirection(center, TakeAimAdapter.getAimPoint(player));
        // Avoid shooting at enemies located too close, as this may cause the turret itself to explode.
        direction.multiply(DEFENCE_DISTANCE / direction.length());

        return center.add(direction);
    }

    private static Block getDirectedBlock(final Block block, final Vector direction) {
        final double dx = Math.abs(direction.getX());
        final double dy = Math.abs(direction.getY());
        final double dz = Math.abs(direction.getZ());

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if(dx > dy) {
            if(dx > dz) {
                x += (int)Math.signum(direction.getX());
            } else {
                z += (int)Math.signum(direction.getZ());
            }
        } else {
            if(dy > dz) {
                y += (int)Math.signum(direction.getY());
            } else {
                z += (int)Math.signum(direction.getZ());
            }
        }

        return block.getWorld().getBlockAt(x, y, z);
    }

    // According to https://minecraft.wiki/w/Waterlogging
    private static final Set<Material> WATER_INHERENT = ImmutableSet.<Material>builder()
            .add(Material.SEAGRASS)
            .add(Material.TALL_SEAGRASS)
            .build();

    private static boolean isBreakable(final Block block, final Turret turret) {
        if((block.getBlockData() instanceof Waterlogged)
                && (((Waterlogged)block.getBlockData()).isWaterlogged())
                || WATER_INHERENT.contains(block.getType())) {
            /*
                According to https://minecraft.wiki/w/Explosion
                the blast resistance of water is 100,
                which none of the turrets can break.
             */
            return false;
        } else {
            return block.getType().getBlastResistance() <= turret.getMaxBlastResistance();
        }
    }
}