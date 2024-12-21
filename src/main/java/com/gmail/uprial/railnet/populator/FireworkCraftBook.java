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
            // With i = 5, generates the same as MineshaftPopulator: 3 and 5
            addRecipe(Material.TNT, i,
                    FireworkEffect.Type.BURST,1 + i / 2, i);
            // With i = 4, generates the same as MineshaftPopulator: 5 and 12
            addRecipe(Material.END_CRYSTAL, i,
                    FireworkEffect.Type.BALL, 1 + i, 3 * i);
            // With i = 1, generates the same as MineshaftPopulator: 10 and 20
            addRecipe(Material.NETHER_STAR, i,
                    FireworkEffect.Type.BALL_LARGE, 10 * i, 20 * i);
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
            final FireworkEffect.Type type,
            final int fireworkPower,
            final int explosionPower) {

        final ItemStack result = new ItemStack(Material.FIREWORK_ROCKET);
        new ItemConfig()
                .firework(type, fireworkPower, explosionPower)
                .apply(result);

        final String key = String.format("e-f-%s-%d-%d", type.toString().toLowerCase(), fireworkPower, explosionPower);
        final NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        final ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, result);

        recipe.addIngredient(1, Material.FIREWORK_ROCKET);
        recipe.addIngredient(amount, material);

        plugin.getServer().addRecipe(recipe);
        addedKeys.add(namespacedKey);
    }
}
