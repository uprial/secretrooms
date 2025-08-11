package com.gmail.uprial.secretrooms.common;

import com.gmail.uprial.secretrooms.populator.ItemConfig;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Formatter {
    public static String format(final Block block) {
        return String.format("%s[%s:%d:%d:%d]%s%s",
                block.getType(),
                block.getWorld().getName(),
                block.getX(), block.getY(), block.getZ(),
                bs2string(block.getState()),
                bd2string(block.getBlockData()));
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

    private static String bs2string(final BlockState bs) {
        if(bs instanceof CreatureSpawner) {
            return String.format("{%s}", ((CreatureSpawner) bs).getSpawnedType());
        } else {
            return "";
        }
    }

    private static String bd2string(final BlockData bd) {
        if((bd instanceof Waterlogged) && ((Waterlogged)bd).isWaterlogged()) {
            return "{water-logged}";
        } else {
            return "";
        }
    }
}
