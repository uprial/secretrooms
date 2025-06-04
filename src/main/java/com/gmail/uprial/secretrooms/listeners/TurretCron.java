package com.gmail.uprial.secretrooms.listeners;

import com.gmail.uprial.secretrooms.SecretRooms;
import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.common.TakeAimAdapter;
import com.gmail.uprial.secretrooms.populator.VirtualChunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

import static com.gmail.uprial.secretrooms.common.Formatter.format;
import static com.gmail.uprial.secretrooms.common.Utils.seconds2ticks;

public class TurretCron extends BukkitRunnable {
    private static final Material HEADING_MATERIAL = Material.HEAVY_CORE;

    private static final int SHOOT_INTERVAL = seconds2ticks(3);

    // Don't shoot across all the map
    private static final double MAX_VIEW_DISTANCE = 32.0D * 16;
    // Don't shoot at too close enemies
    private static final double MIN_VIEW_DISTANCE = 5.0D;

    private final SecretRooms plugin;
    private final CustomLogger customLogger;

    public TurretCron(final SecretRooms plugin,
                      final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;

        runTaskTimer(plugin, SHOOT_INTERVAL, SHOOT_INTERVAL);
    }

    public static void spawn(final VirtualChunk vc, final int x, final int y, final int z) {
        vc.set(x, y - 1, z, Material.OBSIDIAN);
        vc.set(x, y + 1, z, HEADING_MATERIAL);

        final Block crystal = vc.get(x, y, z);

        vc.getWorld().spawnEntity(
                new Location(vc.getWorld(),
                        0.5D + crystal.getX(),
                        0.5D + crystal.getY(),
                        0.5D + crystal.getZ()),
                EntityType.END_CRYSTAL);

        // Defence buildings
        for(int i = -1; i <= +1; i += 2) {
            vc.set(x + i, y, z, Material.BLACK_STAINED_GLASS);
            vc.set(x, y, z + i, Material.BLACK_STAINED_GLASS);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public void run() {
        final Map<UUID, List<Player>> worldsPlayers = new HashMap<>();
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if(player.isValid()) {
                worldsPlayers
                        .computeIfAbsent(player.getWorld().getUID(), (k) -> new ArrayList<>())
                        .add(player);
            }
        }

        for(final World world : plugin.getServer().getWorlds()) {
            if(worldsPlayers.containsKey(world.getUID())) {
                for(final EnderCrystal crystal : world.getEntitiesByClass(EnderCrystal.class)) {
                    if (crystal.isValid() && isTurret(crystal)) {
                        final Player player = getClosestVisiblePlayer(crystal, worldsPlayers.get(world.getUID()));
                        if(player != null) {
                            launch(crystal, player);

                            if (customLogger.isDebugMode()) {
                                customLogger.debug(String.format("%s launched at %s", format(crystal), format(player)));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isTurret(final EnderCrystal crystal) {
        final Location location = crystal.getLocation().clone().add(0.0D, 1.0D, 0.0D);

        // I tested, and getMaxHeight() already can't be set.
        if (location.getBlockY() < crystal.getWorld().getMaxHeight()) {
            return crystal.getWorld().getBlockAt(location).getType().equals(HEADING_MATERIAL);
        } else {
            return false;
        }
    }

    private Player getClosestVisiblePlayer(final EnderCrystal crystal, final List<Player> players) {
        Player closestPlayer = null;
        Double closestDistance = null;

        for (final Player player : players) {
            if(player.isValid()
                    && (!player.isInvisible())
                    && (!player.isInvulnerable())
                    && isSeeingPlayer(crystal, player)) {

                final double distance = getAimPoint(player).distance(crystal.getLocation());

                if ((closestPlayer == null) || (distance < closestDistance)) {
                    closestPlayer = player;
                    closestDistance = distance;
                }
            }
        }

        return closestPlayer;
    }

    private boolean isSeeingPlayer(final EnderCrystal crystal, final Player player) {
        final Location fromLocation = getLaunchPoint(crystal, player);
        final Location toLocation = getAimPoint(player);

        final double distance = toLocation.distance(fromLocation);
        if((distance > MIN_VIEW_DISTANCE) && (distance < MAX_VIEW_DISTANCE)) {
            // Check for direct vision
            final RayTraceResult rayTraceResult = fromLocation.getWorld().rayTraceBlocks(
                    fromLocation,
                    getDirection(fromLocation, toLocation),
                    // -1.0D to avoid colliding with the player itself
                    toLocation.distance(fromLocation) - 1.0D,
                    FluidCollisionMode.ALWAYS);

            return (rayTraceResult == null);
        } else {
            return false;
        }
    }

    private void launch(final EnderCrystal crystal, final Player player) {
        final Location fromLocation = getLaunchPoint(crystal, player);

        /*
            According to https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Ambient.html,
            Bat is the only ambient mob.
         */
        final Mob mob = (Mob)crystal.getWorld().spawnEntity(fromLocation, EntityType.BAT);
        //ghast.getAttribute(Attribute.SCALE).setBaseValue(0.01D);
        //ghast.setCollidable(false);
        //ghast.setAware(false);
        //mob.teleport(fromLocation);

        TakeAimAdapter.setTarget(mob, player);
        mob.launchProjectile(LargeFireball.class);

        mob.remove();
    }

    private Vector getDirection(final Location fromLocation, final Location toLocation) {
        final Location direction = toLocation.clone().subtract(fromLocation);
        final double length = direction.length();

        return new Vector(
                direction.getX() / length,
                direction.getY() / length,
                direction.getZ() / length
        );
    }

    private Location getLaunchPoint(final EnderCrystal crystal, final Player player) {
        final Location bodyLocation = new Location(
                crystal.getLocation().getWorld(),
                crystal.getLocation().getBlockX() + 0.5D,
                crystal.getLocation().getBlockY() + 0.5D,
                crystal.getLocation().getBlockZ() + 0.5D);

        final Vector direction = getDirection(bodyLocation, getAimPoint(player));
        // Give a space for the defence buildings
        direction.multiply(2.0D / direction.length());

        return bodyLocation.add(direction);
    }

    // According to TakeAim:ProjectileHoming
    private Location getAimPoint(final Player player) {
        return player.getEyeLocation();
    }
}