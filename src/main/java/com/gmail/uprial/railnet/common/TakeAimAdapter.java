package com.gmail.uprial.railnet.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;

public class TakeAimAdapter {
    public static void setTarget(final Mob source, final Player target) {
        source.setTarget(target);

        // Fixture for TakeAim
        Bukkit.getPluginManager().callEvent(
                new EntityTargetEvent(source, target,
                        EntityTargetEvent.TargetReason.CLOSEST_PLAYER));
    }
}
