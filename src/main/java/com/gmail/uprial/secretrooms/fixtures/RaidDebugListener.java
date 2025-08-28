package com.gmail.uprial.secretrooms.fixtures;

import com.gmail.uprial.secretrooms.SecretRooms;
import com.gmail.uprial.secretrooms.common.AngerHelper;
import com.gmail.uprial.secretrooms.common.CustomLogger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Raid;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;

import static com.gmail.uprial.secretrooms.common.Formatter.format;

// Used to debug raids
public class RaidDebugListener implements Listener {
    private final SecretRooms plugin;
    private final CustomLogger customLogger;

    public RaidDebugListener(final SecretRooms plugin,
                             final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidFinish(RaidFinishEvent event) {
        broadcast(event.getRaid(), "completed");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidSpawnWave(RaidSpawnWaveEvent event) {
        broadcast(event.getRaid(), "spawned a wave");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidStop(RaidStopEvent event) {
        /*
            stopped: timeout
            stopped: finished
            stopped: unspawnable
            stopped: not in village
         */
        broadcast(event.getRaid(), String.format("stopped: %s",
                event.getReason().toString().toLowerCase().replace("_", " ")));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRaidTrigger(RaidTriggerEvent event) {
        broadcast(event.getRaid(), "triggered");
    }

    private void broadcast(final Raid raid, final String status) {
        final Location location = raid.getLocation();
        for (final Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.isValid()
                    && (p.getWorld().getUID().equals(location.getWorld().getUID()))
                    && (AngerHelper.isSimulated(location, p))) {

                p.sendMessage(ChatColor.YELLOW +
                        String.format("Raid in %.0f block distance %s",
                        location.distance(p.getLocation()), status));
            }
        }
        customLogger.info(String.format("Raid at %s %s", format(location), status));
    }
}
