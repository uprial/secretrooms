package com.gmail.uprial.secretrooms.listeners;

import com.gmail.uprial.secretrooms.SecretRooms;
import com.gmail.uprial.secretrooms.common.AngerHelper;
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
    /*
        According to https://minecraft.wiki/w/Explosion,
        the Heavy Core blast resistance is 30.
     */
    private static final Material HEADING_MATERIAL = Material.HEAVY_CORE;

    private static final int SHOOT_INTERVAL = seconds2ticks(3);

    private static final float EXPLOSION_POWER = 3.0f;
    /*
        Penetrate blocks with small blast resistance.

        According to https://minecraft.wiki/w/Explosion,
        Creeper has power 3 and explodes blocks with up to 9 of blast resistance.

        It might be reduced to the original 1.0 of Fireball.
        According to https://minecraft.wiki/w/Fireball,
        blocks with blast resistance as low as 3.5 survive if hit from the sides or from the top.
     */
    private static final double MAX_BLAST_RESISTANCE = 9.0f;

    /*
        Don't shoot from the inside of the crystal, give a space for the defence buildings.

        The End Crystals move vertically, taking two blocks.
        If the top block is bordered with 4 defence blocks,
        the distance between the End Crystal position and these 4 defence blocks
        might be more than 2.0: (1.5) * 2 ^ 0.5 = 2.12.
     */
    private static final double DEFENCE_DISTANCE = 3.0D;

    /*
        Avoid shooting at enemies located too close, as this may cause the turret itself to explode.

        According to https://minecraft.wiki/w/Explosion,
        Creeper has power 3 and damages blocks in a 5.1 range.

        According to https://minecraft.wiki/w/End_Crystal,
        the weight and height of the End Crystals are 2.0.
     */
    private static final double EXPLOSION_DISTANCE = 5.1D + 2.0D;

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
                            final Fireball fireball = launch(crystal, player);
                            fireball.setYield(EXPLOSION_POWER);
                            if (customLogger.isDebugMode()) {
                                customLogger.debug(String.format("%s launched a %s at %s",
                                        format(crystal), fireball.getType(), format(player)));
                            }
                        }
                    }
                }
            }
        }
    }

    void onExplode(final EnderCrystal crystal) {
        if (isTurret(crystal)) {
            // Break Heavy Core together with its End Crystal
            getHeading(crystal).setType(Material.AIR);
        }
    }

    private boolean isTurret(final EnderCrystal crystal) {
        final Block heading = getHeading(crystal);

        return (heading != null) && (heading.getType().equals(HEADING_MATERIAL));
    }

    private Block getHeading(final EnderCrystal crystal) {
        final Location location = crystal.getLocation().clone().add(0.0D, 1.0D, 0.0D);

        // I tested, and getMaxHeight() already can't be set.
        if (location.getBlockY() < crystal.getWorld().getMaxHeight()) {
            return crystal.getWorld().getBlockAt(location);
        } else {
            return null;
        }
    }

    private Player getClosestVisiblePlayer(final EnderCrystal crystal, final List<Player> players) {
        return AngerHelper.getSmallestItem(players, (final Player player) -> {
            if(AngerHelper.isValidPlayer(player) && isSeeingPlayer(crystal, player)) {
                return TakeAimAdapter.getAimPoint(player).distance(crystal.getLocation());
            } else {
                return null;
            }
        });
    }

    private boolean isSeeingPlayer(final EnderCrystal crystal, final Player player) {
        // Don't shoot across the whole map.
        if(!AngerHelper.isSimulated(crystal, player)) {
            return false;
        }

        final Location fromLocation = getLaunchPoint(crystal, player);
        final Location toLocation = TakeAimAdapter.getAimPoint(player);

        final double distance = toLocation.distance(fromLocation);
        // Too close
        if(distance < EXPLOSION_DISTANCE - DEFENCE_DISTANCE) {
            return false;
        }
        // Check for direct vision
        final RayTraceResult rayTraceResult = AngerHelper.rayTraceBlocks(
                fromLocation,
                toLocation,
                // Fireballs don't care about fluids
                FluidCollisionMode.NEVER);

        // Has no blocks between
        if(rayTraceResult == null) {
            return true;
        }

        // Can break a block between
        return rayTraceResult.getHitBlock().getType().getBlastResistance() <= MAX_BLAST_RESISTANCE;
    }

    private Fireball launch(final EnderCrystal crystal, final Player player) {
        final Location fromLocation = getLaunchPoint(crystal, player);

        /*
            According to https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Ambient.html,
            Bat is the only ambient mob.
         */
        final Mob mob = (Mob)crystal.getWorld().spawnEntity(fromLocation, EntityType.BAT);

        try {
            return TakeAimAdapter.launchFireball(mob, player, Fireball.class);
        } finally {
            mob.remove();
        }
    }

    private Location getLaunchPoint(final EnderCrystal crystal, final Player player) {
        final Location bodyLocation = new Location(
                crystal.getLocation().getWorld(),
                crystal.getLocation().getBlockX() + 0.5D,
                crystal.getLocation().getBlockY() + 0.5D,
                crystal.getLocation().getBlockZ() + 0.5D);

        final Vector direction = AngerHelper.getDirection(bodyLocation, TakeAimAdapter.getAimPoint(player));
        // Avoid shooting at enemies located too close, as this may cause the turret itself to explode.
        direction.multiply(DEFENCE_DISTANCE / direction.length());

        return bodyLocation.add(direction);
    }
}