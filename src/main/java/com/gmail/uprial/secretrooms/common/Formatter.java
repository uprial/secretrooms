package com.gmail.uprial.secretrooms.common;

import com.gmail.uprial.secretrooms.populator.ItemConfig;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Formatter {
    public static String format(final Block block) {
        return String.format("%s[%s:%d:%d:%d]",
                block.getType(),
                block.getWorld().getName(),
                block.getX(), block.getY(), block.getZ());
    }

    public static String format(final Entity entity) {
        if (entity instanceof Player) {
            return format((Player) entity);
        }
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                entity.getType(),
                entity.getWorld().getName(),
                entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
    }

    public static String format(final Player player) {
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                player.getName(),
                player.getWorld().getName(),
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public static String format(final Location location) {
        return String.format("%s:%.0f:%.0f:%.0f",
                (location.getWorld() != null) ? location.getWorld().getName() : "empty",
                location.getX(), location.getY(), location.getZ());
    }

    public static String format(final Chunk chunk) {
        return String.format("%s:%d:%d",
                chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static String format(final ItemStack itemStack) {
        return ItemConfig.format(itemStack);
    }
}
