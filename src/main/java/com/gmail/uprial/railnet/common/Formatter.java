package com.gmail.uprial.railnet.common;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Formatter {
    public static String format(Entity entity) {
        if(entity == null) {
            return "null";
        }
        Location location = entity.getLocation();
        return String.format("%s[w: %s, x: %.0f, y: %.0f, z: %.0f, hp: %.2f, id: %s]",
                entity.getType(),
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ(),
                (entity instanceof LivingEntity) ? ((LivingEntity)entity).getHealth() : -1,
                entity.getUniqueId());
    }

    public static String format(Location location) {
        if(location == null) {
            return "null";
        }
        return String.format("[w: %s, x: %.0f, y: %.0f, z: %.0f]",
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ());
    }
}