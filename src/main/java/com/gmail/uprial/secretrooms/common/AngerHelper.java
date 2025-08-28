package com.gmail.uprial.secretrooms.common;

import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
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
    public static boolean isSimulated(final Location location, final Player player) {
        final Chunk entityChunk = location.getChunk();
        final Chunk playerChunk = player.getLocation().getChunk();
        final int simulationDistance = player.getWorld().getSimulationDistance();

        return (
                    // Fail fast without Math.sqrt() and Math.pow()
                    playerChunk.getX() - entityChunk.getX()
                    +
                    playerChunk.getZ() - entityChunk.getZ()
                    <=
                    simulationDistance * 2
                ) && (
                    Math.sqrt(
                        Math.pow(playerChunk.getX() - entityChunk.getX(), 2.0D)
                        +
                        Math.pow(playerChunk.getZ() - entityChunk.getZ(), 2.0D)
                    ) <= simulationDistance
                );
    }

    private static boolean isInvisiblePlayer(final Player player) {
        if (!player.isInvisible()) {
            return false;
        }
        final EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return true;
        }
        return (isEmptyMaterial(equipment.getItemInMainHand())
                && isEmptyMaterial(equipment.getItemInOffHand())
                && isEmptyMaterial(equipment.getHelmet())
                && isEmptyMaterial(equipment.getChestplate())
                && isEmptyMaterial(equipment.getLeggings())
                && isEmptyMaterial(equipment.getBoots()));
    }

    private static boolean isEmptyMaterial(final ItemStack itemStack) {
        return (itemStack == null) || (itemStack.getType().equals(Material.AIR));
    }
}
