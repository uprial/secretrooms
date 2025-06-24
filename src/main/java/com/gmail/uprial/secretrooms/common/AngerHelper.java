package com.gmail.uprial.secretrooms.common;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.function.Function;

public class AngerHelper {
    public static <T> T getSmallestItem(
            final Collection<T> items,
            final Function<T, Double> valueSupplier) {

        T smallestItem = null;
        Double smallestValue = null;

        for (final T item : items) {
            final Double value = valueSupplier.apply(item);
            if ((value != null) && ((smallestItem == null) || (value < smallestValue))) {
                smallestItem = item;
                smallestValue = value;
            }
        }

        return smallestItem;
    }

    public static RayTraceResult rayTraceBlocks(final Location fromLocation,
                                                final Location toLocation,
                                                final FluidCollisionMode mode) {
        return fromLocation.getWorld().rayTraceBlocks(
                fromLocation,
                getDirection(fromLocation, toLocation),
                // -1.0D to avoid colliding with the fromLocation itself
                toLocation.distance(fromLocation) - 1.0D,
                mode);
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

    public static boolean isValidPlayer(final Player player) {
        return (player.isValid()
                && (!isInvisiblePlayer(player))
                && (!player.isInvulnerable()));
    }

    /*
        According to https://minecraft.wiki/w/Server.properties,
        max view-distance and simulation-distance are both 32 chunks.
     */
    public static boolean isSimulated(final Entity entity, final Player player) {
        return Math.sqrt(
                Math.pow(player.getLocation().getChunk().getX()
                        - entity.getLocation().getChunk().getX(), 2.0D)
                + Math.pow(player.getLocation().getChunk().getZ()
                        - entity.getLocation().getChunk().getZ(), 2.0D))
                <= player.getWorld().getSimulationDistance();
    }

    private static boolean isInvisiblePlayer(final Player player) {
        if (!player.isInvisible()) {
            return false;
        }
        if (player.getEquipment() == null) {
            return true;
        }
        return (isEmptyMaterial(player.getEquipment().getItemInMainHand())
                && isEmptyMaterial(player.getEquipment().getItemInOffHand())
                && isEmptyMaterial(player.getEquipment().getHelmet())
                && isEmptyMaterial(player.getEquipment().getChestplate())
                && isEmptyMaterial(player.getEquipment().getLeggings())
                && isEmptyMaterial(player.getEquipment().getBoots()));
    }

    private static boolean isEmptyMaterial(final ItemStack itemStack) {
        return (itemStack == null) || (itemStack.getType().equals(Material.AIR));
    }
}
