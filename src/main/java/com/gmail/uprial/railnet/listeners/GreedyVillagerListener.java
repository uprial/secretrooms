package com.gmail.uprial.railnet.listeners;

import com.gmail.uprial.railnet.common.CustomLogger;
import com.google.common.collect.ImmutableMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.*;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class GreedyVillagerListener implements Listener {
    private final CustomLogger customLogger;

    public GreedyVillagerListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private final Map<Enchantment,Enchantment> enchantmentMutators = ImmutableMap.<Enchantment,Enchantment>builder()
            .put(Enchantment.PROTECTION, Enchantment.THORNS)
            .put(Enchantment.MENDING, Enchantment.THORNS)
            .build();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if((!event.isCancelled()) && (event.getRightClicked() instanceof Villager)) {
            final Villager villager = (Villager)event.getRightClicked();

            final List<MerchantRecipe> updatedRecipes = new ArrayList<>();
            boolean updated = false;
            for(final MerchantRecipe recipe : villager.getRecipes()) {

                final ItemStack result = recipe.getResult();
                final Map<Enchantment,Integer> enchantments
                        = ((result.getItemMeta() instanceof EnchantmentStorageMeta))
                        ? ((EnchantmentStorageMeta) result.getItemMeta()).getStoredEnchants()
                        : result.getEnchantments();

                for(Map.Entry<Enchantment,Integer> entry : enchantments.entrySet()) {
                    final Enchantment mutator = enchantmentMutators.get(entry.getKey());
                    if(mutator != null) {
                        if(customLogger.isDebugMode()) {
                            customLogger.debug(String.format("Updating %s recipes for %s: changing %s-%d to %s-%d...",
                                    format(villager), result.getType(),
                                    entry.getKey().getName(), entry.getValue(),
                                    mutator.getName(), entry.getValue()));
                        }

                        if(result.getItemMeta() instanceof EnchantmentStorageMeta) {
                            final EnchantmentStorageMeta itemMeta = (EnchantmentStorageMeta)result.getItemMeta();
                            itemMeta.removeStoredEnchant(entry.getKey());
                            itemMeta.addStoredEnchant(mutator, entry.getValue(), false);
                            result.setItemMeta(itemMeta);
                        } else {
                            result.removeEnchantment(entry.getKey());
                            result.addEnchantment(mutator, entry.getValue());
                        }

                        updated = true;
                    }
                }

                final MerchantRecipe updatedRecipe = new MerchantRecipe(
                        result,
                        recipe.getUses(),
                        recipe.getMaxUses(),
                        recipe.hasExperienceReward(),
                        recipe.getVillagerExperience(),
                        recipe.getPriceMultiplier(),
                        recipe.getDemand(),
                        recipe.getSpecialPrice()
                );
                updatedRecipe.setIngredients(recipe.getIngredients());

                updatedRecipes.add(updatedRecipe);
            }
            if(updated) {
                villager.setRecipes(updatedRecipes);

                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("Updated recipes for %s", format(villager)));
                }
            }
        }
    }
}
