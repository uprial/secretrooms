package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.TakeAimAdapter;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class AngryShooterListener implements Listener {
    private final CustomLogger customLogger;

    public AngryShooterListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled() && (
                event.getEntity() instanceof Skeleton
                        || event.getEntity() instanceof Stray
                        || event.getEntity() instanceof Bogged
                        || event.getEntity() instanceof Breeze
                        || event.getEntity() instanceof Ghast
                        || event.getEntity() instanceof Blaze)) {

            final Mob mob = (Mob)event.getEntity();
            final Player player = getClosestVisiblePlayer(mob);

            if(player != null) {
                TakeAimAdapter.setTarget(mob, player);

                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s targeted at %s", format(mob), format(player)));
                }
            }
        }
    }

    private Player getClosestVisiblePlayer(final Mob mob) {
        Player closestPlayer = null;
        Double closestDistance = null;

        for (final Player player : mob.getWorld().getEntitiesByClass(Player.class)) {
            if(isMonsterSeeingPlayer(mob, player)) {
                final double distance = getAimPoint(player).distance(getAimPoint(mob));

                if ((closestPlayer == null) || (distance < closestDistance)) {
                    closestPlayer = player;
                    closestDistance = distance;
                }
            }
        }

        return closestPlayer;
    }

    private boolean isMonsterSeeingPlayer(final Mob mob, final Player player) {
        final Location fromLocation = getAimPoint(mob);
        final Location toLocation = getAimPoint(player);
        // Check for direct vision
        final RayTraceResult rayTraceResult = fromLocation.getWorld().rayTraceBlocks(
                fromLocation,
                getDirection(fromLocation, toLocation),
                // -1.0D to avoid colliding with the player itself
                toLocation.distance(fromLocation) - 1.0D,
                FluidCollisionMode.ALWAYS);

        return (rayTraceResult == null);
    }

    // According to TakeAim:ProjectileHoming
    private Location getAimPoint(final LivingEntity targetEntity) {
        return targetEntity.getEyeLocation();
    }

    public static Vector getDirection(final Location fromLocation, final Location toLocation) {
        final Location direction = toLocation.clone().subtract(fromLocation);
        final double length = direction.length();

        return new Vector(
                direction.getX() / length,
                direction.getY() / length,
                direction.getZ() / length
        );
    }
}
