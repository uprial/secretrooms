package com.gmail.uprial.railnet.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

public class AimHelper {
    public static Vector getDirection(final Location fromLocation, final Location toLocation) {
        final Location direction = toLocation.clone().subtract(fromLocation);
        final double length = direction.length();

        return new Vector(
                direction.getX() / length,
                direction.getY() / length,
                direction.getZ() / length
        );
    }

    public static void setTarget(final Mob source, final Player target) {
        source.setTarget(target);

        // Fixture for TakeAim
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(source, target,
                        EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
    }
}
