package com.gmail.uprial.secretrooms.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.util.Vector;

public class TakeAimAdapter {
    public static void setTarget(final Mob source, final Player target) {
        source.setTarget(target);

        // Send an event to TakeAim, which Mob::setTarget does not.
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(source, target,
                        EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
    }

    public static <T extends Fireball> T launchFireball(final Mob source,
                                                        final Player target,
                                                        Class<? extends T> tFireball) {

        if(null != Bukkit.getPluginManager().getPlugin("TakeAim")) {
            setTarget(source, target);
            /*
                I carefully tested, and TakeAim works without this fixture.

                I decided to keep this comment in case I forget I tested.

            Bukkit.getPluginManager().callEvent(
                    new ProjectileLaunchEvent(dragonFireball));
             */
            return source.launchProjectile(tFireball);
        } else {
            T fireball = source.launchProjectile(tFireball);

            final Location targetLocation = getAimPoint(target);
            targetLocation.subtract(fireball.getLocation());

            final Vector newAcceleration = targetLocation.toVector();
            newAcceleration.multiply(fireball.getAcceleration().length() / targetLocation.length());

            fireball.setAcceleration(newAcceleration);

            return fireball;
        }
    }

    // According to TakeAim:ProjectileHoming
    public static Location getAimPoint(final Player targetPlayer) {
        return targetPlayer.getLocation()
                .add(targetPlayer.getEyeLocation())
                .multiply(0.5D);
    }
}
