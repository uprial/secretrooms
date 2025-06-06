package com.gmail.uprial.secretrooms.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

public class TakeAimAdapter {
    public static boolean hasPlugin() {
        return (null != Bukkit.getPluginManager().getPlugin("TakeAim"));
    }

    public static void setTarget(final Mob source, final Player target) {
        source.setTarget(target);

        // Fixture for TakeAim
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(source, target,
                        EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
    }
}
