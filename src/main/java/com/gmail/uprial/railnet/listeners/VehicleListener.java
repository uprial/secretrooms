package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class VehicleListener implements Listener {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    public VehicleListener(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if(!event.isCancelled() && (event.getVehicle() instanceof Minecart)) {
            final Minecart minecart = (Minecart)event.getVehicle();
            minecart.setMaxSpeed(plugin.getRailNetConfig().getMinecartMaxSpeed());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if(event.getVehicle() instanceof Minecart) {
            final Minecart minecart = (Minecart)event.getVehicle();

            final Location location = minecart.getLocation();
            location.setY(location.getBlockY() - 1);

            /*customLogger.debug(String.format("Speed of %s: %.2f - %.2f",
                    minecart.getUniqueId().toString().substring(0, 4),
                    minecart.getVelocity().getX(),
                    minecart.getVelocity().getZ()));*/
            if(minecart.getWorld().getBlockAt(location).getType().equals(
                    plugin.getRailNetConfig().getMinecartSlowBlock()
            )) {
                minecart.setMaxSpeed(plugin.getRailNetConfig().getMinecartSlowSpeed());
            } else {
                minecart.setMaxSpeed(plugin.getRailNetConfig().getMinecartMaxSpeed());
            }
        }
    }
}