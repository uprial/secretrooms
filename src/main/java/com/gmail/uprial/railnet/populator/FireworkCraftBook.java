package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.RailNet;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashSet;
import java.util.Set;

public class FireworkCraftBook {
    private final RailNet plugin;

    private final Set<NamespacedKey> addedKeys = new HashSet<>();

    public FireworkCraftBook(final RailNet plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        for(int i = 1; i < 9; i++) {
            addRecipe(Material.TNT, i, 1 + i / 2, i);
            addRecipe(Material.NETHER_STAR, i,10 * i, 20 * i);
        }
    }

    public void disable() {
        for(NamespacedKey key : addedKeys) {
            plugin.getServer().removeRecipe(key);
        }
    }

    private void addRecipe(
            final Material material,
            final int amount,
            final int fireworkPower,
            final int explosionPower) {

        final ItemStack result = new ItemStack(Material.FIREWORK_ROCKET);
        new ItemConfig()
                .firework(FireworkEffect.Type.BURST, fireworkPower, explosionPower)
                .apply(result);

        final String key = String.format("explosive-firework-%d-%d", fireworkPower, explosionPower);
        final NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        final ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, result);

        recipe.addIngredient(1, Material.FIREWORK_ROCKET);
        recipe.addIngredient(amount, material);

        plugin.getServer().addRecipe(recipe);
        addedKeys.add(namespacedKey);
    }
}
