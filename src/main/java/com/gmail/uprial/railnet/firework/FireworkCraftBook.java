package com.gmail.uprial.railnet.firework;

import com.gmail.uprial.railnet.RailNet;
import com.google.common.collect.ImmutableMap;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class FireworkCraftBook {
    private final RailNet plugin;

    private final Map<Material, EntityType> egg2entity = ImmutableMap.<Material,EntityType>builder()
            .put(Material.ARMADILLO_SPAWN_EGG, EntityType.ARMADILLO)
            .put(Material.ALLAY_SPAWN_EGG, EntityType.ALLAY)
            .put(Material.AXOLOTL_SPAWN_EGG, EntityType.AXOLOTL)
            .put(Material.BAT_SPAWN_EGG, EntityType.BAT)
            .put(Material.BEE_SPAWN_EGG, EntityType.BEE)
            .put(Material.BLAZE_SPAWN_EGG, EntityType.BLAZE)
            .put(Material.BOGGED_SPAWN_EGG, EntityType.BOGGED)
            .put(Material.BREEZE_SPAWN_EGG, EntityType.BREEZE)
            .put(Material.CAT_SPAWN_EGG, EntityType.CAT)
            .put(Material.CAMEL_SPAWN_EGG, EntityType.CAMEL)
            .put(Material.CAVE_SPIDER_SPAWN_EGG, EntityType.CAVE_SPIDER)
            .put(Material.CHICKEN_SPAWN_EGG, EntityType.CHICKEN)
            .put(Material.CREAKING_SPAWN_EGG, EntityType.CREAKING)
            .put(Material.COD_SPAWN_EGG, EntityType.COD)
            .put(Material.COW_SPAWN_EGG, EntityType.COW)
            .put(Material.CREEPER_SPAWN_EGG, EntityType.CREEPER)
            .put(Material.DOLPHIN_SPAWN_EGG, EntityType.DOLPHIN)
            .put(Material.DONKEY_SPAWN_EGG, EntityType.DONKEY)
            .put(Material.DROWNED_SPAWN_EGG, EntityType.DROWNED)
            .put(Material.ELDER_GUARDIAN_SPAWN_EGG, EntityType.ELDER_GUARDIAN)
            .put(Material.ENDER_DRAGON_SPAWN_EGG, EntityType.ENDER_DRAGON)
            .put(Material.ENDERMAN_SPAWN_EGG, EntityType.ENDERMAN)
            .put(Material.ENDERMITE_SPAWN_EGG, EntityType.ENDERMITE)
            .put(Material.EVOKER_SPAWN_EGG, EntityType.EVOKER)
            .put(Material.FOX_SPAWN_EGG, EntityType.FOX)
            .put(Material.FROG_SPAWN_EGG, EntityType.FROG)
            .put(Material.GHAST_SPAWN_EGG, EntityType.GHAST)
            .put(Material.GLOW_SQUID_SPAWN_EGG, EntityType.GLOW_SQUID)
            .put(Material.GOAT_SPAWN_EGG, EntityType.GOAT)
            .put(Material.GUARDIAN_SPAWN_EGG, EntityType.GUARDIAN)
            .put(Material.HOGLIN_SPAWN_EGG, EntityType.HOGLIN)
            .put(Material.HORSE_SPAWN_EGG, EntityType.HORSE)
            .put(Material.HUSK_SPAWN_EGG, EntityType.HUSK)
            .put(Material.IRON_GOLEM_SPAWN_EGG, EntityType.IRON_GOLEM)
            .put(Material.LLAMA_SPAWN_EGG, EntityType.LLAMA)
            .put(Material.MAGMA_CUBE_SPAWN_EGG, EntityType.MAGMA_CUBE)
            .put(Material.MOOSHROOM_SPAWN_EGG, EntityType.MOOSHROOM)
            .put(Material.MULE_SPAWN_EGG, EntityType.MULE)
            .put(Material.OCELOT_SPAWN_EGG, EntityType.OCELOT)
            .put(Material.PANDA_SPAWN_EGG, EntityType.PANDA)
            .put(Material.PARROT_SPAWN_EGG, EntityType.PARROT)
            .put(Material.PHANTOM_SPAWN_EGG, EntityType.PHANTOM)
            .put(Material.PIG_SPAWN_EGG, EntityType.PIG)
            .put(Material.PIGLIN_SPAWN_EGG, EntityType.PIGLIN)
            .put(Material.PIGLIN_BRUTE_SPAWN_EGG, EntityType.PIGLIN_BRUTE)
            .put(Material.PILLAGER_SPAWN_EGG, EntityType.PILLAGER)
            .put(Material.POLAR_BEAR_SPAWN_EGG, EntityType.POLAR_BEAR)
            .put(Material.PUFFERFISH_SPAWN_EGG, EntityType.PUFFERFISH)
            .put(Material.RABBIT_SPAWN_EGG, EntityType.RABBIT)
            .put(Material.RAVAGER_SPAWN_EGG, EntityType.RAVAGER)
            .put(Material.SALMON_SPAWN_EGG, EntityType.SALMON)
            .put(Material.SHEEP_SPAWN_EGG, EntityType.SHEEP)
            .put(Material.SHULKER_SPAWN_EGG, EntityType.SHULKER)
            .put(Material.SILVERFISH_SPAWN_EGG, EntityType.SILVERFISH)
            .put(Material.SKELETON_SPAWN_EGG, EntityType.SKELETON)
            .put(Material.SKELETON_HORSE_SPAWN_EGG, EntityType.SKELETON_HORSE)
            .put(Material.SLIME_SPAWN_EGG, EntityType.SLIME)
            .put(Material.SNIFFER_SPAWN_EGG, EntityType.SNIFFER)
            .put(Material.SNOW_GOLEM_SPAWN_EGG, EntityType.SNOW_GOLEM)
            .put(Material.SPIDER_SPAWN_EGG, EntityType.SPIDER)
            .put(Material.SQUID_SPAWN_EGG, EntityType.SQUID)
            .put(Material.STRAY_SPAWN_EGG, EntityType.STRAY)
            .put(Material.STRIDER_SPAWN_EGG, EntityType.STRIDER)
            .put(Material.TADPOLE_SPAWN_EGG, EntityType.TADPOLE)
            .put(Material.TRADER_LLAMA_SPAWN_EGG, EntityType.TRADER_LLAMA)
            .put(Material.TROPICAL_FISH_SPAWN_EGG, EntityType.TROPICAL_FISH)
            .put(Material.TURTLE_SPAWN_EGG, EntityType.TURTLE)
            .put(Material.VEX_SPAWN_EGG, EntityType.VEX)
            .put(Material.VILLAGER_SPAWN_EGG, EntityType.VILLAGER)
            .put(Material.VINDICATOR_SPAWN_EGG, EntityType.VINDICATOR)
            .put(Material.WANDERING_TRADER_SPAWN_EGG, EntityType.WANDERING_TRADER)
            .put(Material.WARDEN_SPAWN_EGG, EntityType.WARDEN)
            .put(Material.WITCH_SPAWN_EGG, EntityType.WITCH)
            .put(Material.WITHER_SPAWN_EGG, EntityType.WITHER)
            .put(Material.WITHER_SKELETON_SPAWN_EGG, EntityType.WITHER_SKELETON)
            .put(Material.WOLF_SPAWN_EGG, EntityType.WOLF)
            .put(Material.ZOGLIN_SPAWN_EGG, EntityType.ZOGLIN)
            .put(Material.ZOMBIE_SPAWN_EGG, EntityType.ZOMBIE)
            .put(Material.ZOMBIE_HORSE_SPAWN_EGG, EntityType.ZOMBIE_HORSE)
            .put(Material.ZOMBIE_VILLAGER_SPAWN_EGG, EntityType.ZOMBIE_VILLAGER)
            .put(Material.ZOMBIFIED_PIGLIN_SPAWN_EGG, EntityType.ZOMBIFIED_PIGLIN)
            .build();

    private final Set<NamespacedKey> addedKeys = new HashSet<>();

    FireworkCraftBook(final RailNet plugin) {
        this.plugin = plugin;
    }

    void enable() {
        for(int i = 1; i < 9; i++) {
            // With i = 5, generates the same as MineshaftPopulator: 3 and 5
            addRecipe(Material.TNT, i,
                    FireworkEffect.Type.BURST,1 + i / 2, i);
            // With i = 6, generates the same as MineshaftPopulator: 7 and 12
            addRecipe(Material.END_CRYSTAL, i,
                    FireworkEffect.Type.BALL, 1 + i, 2 * i);
            // With i = 1, generates the same as MineshaftPopulator: 10 and 20
            addRecipe(Material.NETHER_STAR, i,
                    FireworkEffect.Type.BALL_LARGE, 10 * i, 20 * i);

            for(Map.Entry<Material, EntityType> entry : egg2entity.entrySet()) {
                addRecipe(entry.getKey(), i,
                        FireworkEffect.Type.STAR, 1 + i / 2, entry.getValue(), i);
            }
        }
    }

    void disable() {
        for(NamespacedKey key : addedKeys) {
            plugin.getServer().removeRecipe(key);
        }
        addedKeys.clear();
    }

    private void addRecipe(
            final Material material,
            final int amount,
            final FireworkEffect.Type type,
            final int power,
            final EntityType entityType,
            final int entityAmount) {

        final ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkEngine.apply(itemStack, type, power, entityType, entityAmount);

        addRecipe(material, amount, itemStack);
    }

    private void addRecipe(
            final Material material,
            final int amount,
            final FireworkEffect.Type type,
            final int power,
            final int explosionPower) {

        final ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkEngine.apply(itemStack, type, power, explosionPower);

        addRecipe(material, amount, itemStack);
    }

    private void addRecipe(
            final Material material,
            final int amount,
            final ItemStack itemStack) {

        final String key = String.format("e-f-%s-%d", material.toString().toLowerCase(), amount);
        final NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        final ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, itemStack);

        recipe.addIngredient(1, Material.FIREWORK_ROCKET);
        recipe.addIngredient(amount, material);

        plugin.getServer().addRecipe(recipe);
        addedKeys.add(namespacedKey);
    }
}
