package com.gmail.uprial.secretrooms.listeners;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

import static com.gmail.uprial.secretrooms.common.Formatter.format;

public class InventoryCleanupListener implements Listener {
    private final CustomLogger customLogger;

    public InventoryCleanupListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private static final Set<Material> CLEANUP_SET = ImmutableSet.<Material>builder()
            .add(Material.SPAWNER)
            .build();

    // Our plugin has the last word on the world population.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && (event.getClickedBlock().getState() instanceof InventoryHolder)) {

            remove(format(event.getPlayer()),
                    event.getPlayer().getInventory());
            remove(String.format("%s opened by %s", format(event.getClickedBlock()), format(event.getPlayer())),
                    ((InventoryHolder)event.getClickedBlock().getState()).getInventory());
        }
    }

    public void remove(final String title, final Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null) {
                if(CLEANUP_SET.contains(itemStack.getType())) {
                    customLogger.warning(String.format("Removed %sx%d from %s inventory slot #%d",
                            itemStack.getType(), itemStack.getAmount(), title, i));
                    inventory.setItem(i, null);
                }
            }
        }
    }
}
