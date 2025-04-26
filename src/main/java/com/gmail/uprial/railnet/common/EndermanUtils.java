package com.gmail.uprial.railnet.common;

import org.bukkit.entity.Player;

public class EndermanUtils {
    public static boolean isAppropriatePlayer(final Player player) {
        return (player.isValid())
                && (!player.isFlying()) && (!player.isGliding())
                && (!player.isInvisible()) && (!player.isInvulnerable())
                && (player.getWorld()
                .getBlockAt(player.getLocation().clone().add(0.0D, 2.0D, 0.0D)).isPassable());
    }
}
