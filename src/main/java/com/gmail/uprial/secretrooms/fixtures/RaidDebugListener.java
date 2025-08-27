package com.gmail.uprial.secretrooms.fixtures;

import com.gmail.uprial.secretrooms.SecretRooms;
import org.bukkit.ChatColor;
import org.bukkit.Raid;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

// Used to debug raids
public class RaidDebugListener implements Listener {
    private final SecretRooms plugin;

    public RaidDebugListener(final SecretRooms plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidFinish(RaidFinishEvent event) {
        broadcast(String.format("%s completed", format(event.getRaid())));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidSpawnWave(RaidSpawnWaveEvent event) {
        broadcast(String.format("%s spawned a wave", format(event.getRaid())));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidStop(RaidStopEvent event) {
        broadcast(String.format("%s stopped: %s", format(event.getRaid()), event.getReason()));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidTrigger(RaidTriggerEvent event) {
        broadcast(String.format("%s triggered", format(event.getRaid())));
    }

    private void broadcast(final String message) {
        plugin.getServer().broadcastMessage(ChatColor.YELLOW + message);
    }

    private String format(final Raid raid) {
        return String.format("Raid at %d:%d",
                raid.getLocation().getBlockX(), raid.getLocation().getBlockZ());
    }
}
