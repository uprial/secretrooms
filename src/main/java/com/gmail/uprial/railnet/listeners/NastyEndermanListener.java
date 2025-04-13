package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class NastyEndermanListener implements Listener {
    private static final double PROBABILITY = 0.1D;

    private final CustomLogger customLogger;

    public NastyEndermanListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private Player getStrongestPlayer(final World world) {
        Player strongestPlayer = null;

        for (final Player player : world.getEntitiesByClass(Player.class)) {
            if ((strongestPlayer == null || strongestPlayer.getHealth() < player.getHealth())
                    && (isAppropriatePlayer(player))) {
                strongestPlayer = player;
            }
        }

        return strongestPlayer;
    }

    private boolean isAppropriatePlayer(final Player player) {
        return (player.isValid()) && (!player.isFlying()) && (!player.isGliding())
            && (player.getWorld()
                .getBlockAt(player.getLocation().clone().add(0.0D, 2.0D, 0.0D)).isPassable());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && event.getEntity().getType().equals(EntityType.ENDERMAN)
                && (Probability.PASS(PROBABILITY, 0))) {

            final Player player = getStrongestPlayer(event.getEntity().getWorld());

            if(player != null) {
                final Enderman enderman = (Enderman) event.getEntity();

                if ((player.getEquipment() != null)
                        && (player.getEquipment().getHelmet() != null)
                        && (player.getEquipment().getHelmet().getType().equals(Material.CARVED_PUMPKIN))) {

                    // Enderman takes the player helmet
                    player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                    enderman.setCarriedBlock(Material.CARVED_PUMPKIN.createBlockData());
                }

                enderman.teleport(player.getLocation());
                enderman.setTarget(player);

                customLogger.info(String.format("%s targeted at %s with %.2f health",
                        format(enderman), format(player), player.getHealth()));
            }
        }
    }
}