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

import static com.gmail.uprial.railnet.common.DoubleHelper.MIN_DOUBLE_VALUE;
import static com.gmail.uprial.railnet.common.Formatter.format;

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
            update((Minecart)event.getVehicle(), plugin.getRailNetConfig().getMinecartMaxSpeed());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleMove(VehicleMoveEvent event) {
        if(event.getVehicle() instanceof Minecart) {
            final Minecart minecart = (Minecart)event.getVehicle();

            final Location location = minecart.getLocation();
            location.setY(location.getBlockY() - 1);

            if(minecart.getWorld().getBlockAt(location).getType().equals(
                    plugin.getRailNetConfig().getMinecartSlowBlock()
            )) {
                update(minecart, plugin.getRailNetConfig().getMinecartSlowSpeed());
            } else {
                update(minecart, plugin.getRailNetConfig().getMinecartMaxSpeed());
            }
        }
    }

    private void update(final Minecart minecart, final double newMaxSpeed) {
        if(Math.abs(minecart.getMaxSpeed() - newMaxSpeed) > MIN_DOUBLE_VALUE) {
            if(customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s %.2f > %.2f",
                        format(minecart),
                        minecart.getMaxSpeed(),
                        newMaxSpeed));
            }
            minecart.setMaxSpeed(newMaxSpeed);
        }
    }
}