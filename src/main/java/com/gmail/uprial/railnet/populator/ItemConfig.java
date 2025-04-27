package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.RandomUtils;
import com.gmail.uprial.railnet.firework.FireworkEngine;
import com.google.common.collect.ImmutableList;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemConfig {
    private final static Random RANDOM = new Random();

    private interface VirtualItemConfig {
        void apply(final ItemStack itemStack);
    }

    private final ImmutableList<VirtualItemConfig> configs;

    private static class ench1 implements VirtualItemConfig {
        final Enchantment enchantment;
        final int level;

        ench1(final Enchantment enchantment, final int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            itemStack.addUnsafeEnchantment(enchantment, level);
        }
    }

    private static class ench2 implements VirtualItemConfig {
        final Enchantment enchantment;
        final int level1;
        final int level2;

        ench2(final Enchantment enchantment, final int level1, final int level2) {
            this.enchantment = enchantment;
            this.level1 = level1;
            this.level2 = level2;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            itemStack.addUnsafeEnchantment(enchantment, level1 + RANDOM.nextInt(level2 - level1 + 1));
        }
    }

    private static class trim implements VirtualItemConfig {
        final TrimMaterial material;
        final TrimPattern pattern;

        trim(final TrimMaterial material, final TrimPattern pattern) {
            this.material = material;
            this.pattern = pattern;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            final ArmorMeta armorMeta = (ArmorMeta) itemStack.getItemMeta();
            armorMeta.setTrim(new ArmorTrim(material, pattern));
            itemStack.setItemMeta(armorMeta);
        }
    }

    private static class amplifier implements VirtualItemConfig {
        final int level;

        amplifier(final int level) {
            this.level = level;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            final OminousBottleMeta ominousBottleMeta = (OminousBottleMeta) itemStack.getItemMeta();
            ominousBottleMeta.setAmplifier(level);
            itemStack.setItemMeta(ominousBottleMeta);
        }
    }

    private static class effects implements VirtualItemConfig {
        final Map<PotionEffectType, Integer> effectTypeOptions;
        final Set<Integer> durationOptions;

        effects(final Set<Integer> durationOptions, final Map<PotionEffectType, Integer> effectTypeOptions) {
            this.durationOptions = durationOptions;
            this.effectTypeOptions = effectTypeOptions;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            final PotionEffectType effectType = RandomUtils.getSetItem(effectTypeOptions.keySet());

            final int amplifier = effectTypeOptions.get(effectType);

            int duration = RandomUtils.getSetItem(durationOptions);
            if (itemStack.getType().equals(Material.TIPPED_ARROW)) {
                /*
                    According to https://minecraft.wiki/w/Tipped_Arrow,
                    The duration of the effect is 1⁄8 that of the corresponding potion.
                 */
                duration *= 8;
            }

            new effect(effectType, duration, amplifier).apply(itemStack);
        }
    }

    private static class effect implements VirtualItemConfig {
        final PotionEffectType effectType;
        final Integer duration;
        final Integer amplifier;

        effect(final PotionEffectType effectType, final Integer duration, final Integer amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

            potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            itemStack.setItemMeta(potionMeta);
        }
    }

    private static class firework implements VirtualItemConfig {
        final FireworkEffect.Type type;
        final int fireworkPower;
        final int explosionPower;

        firework(final FireworkEffect.Type type, final int fireworkPower, final int explosionPower) {
            this.type = type;
            this.fireworkPower = fireworkPower;
            this.explosionPower = explosionPower;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            FireworkEngine.apply(itemStack, type, fireworkPower, explosionPower);
        }
    }

    public ItemConfig() {
        configs = ImmutableList.<VirtualItemConfig>builder().build();
    }

    private ItemConfig(final ImmutableList<VirtualItemConfig> configs) {
        this.configs = configs;
    }

    private ItemConfig addConfig(final VirtualItemConfig config) {
        return new ItemConfig(
                ImmutableList.<VirtualItemConfig>builder()
                        .addAll(configs)
                        .add(config)
                        .build());
    }

    public ItemConfig ench(final Enchantment enchantment) {
        return addConfig(new ench1(enchantment, 1));
    }

    public ItemConfig ench(final Enchantment enchantment, final int level1, final int level2) {
        return addConfig(new ench2(enchantment, level1, level2));
    }

    public ItemConfig trim(final TrimMaterial material, final TrimPattern pattern) {
        return addConfig(new trim(material, pattern));
    }

    public ItemConfig amplify(final int level) {
        return addConfig(new amplifier(level));
    }

    public ItemConfig effects(final Set<Integer> durationOptions, final Map<PotionEffectType,Integer> effectTypeOptions) {
        return addConfig(new effects(durationOptions, effectTypeOptions));
    }

    public ItemConfig firework(final FireworkEffect.Type type, final int fireworkPower, final int explosionPower) {
        return addConfig(new firework(type, fireworkPower, explosionPower));
    }

    public ItemConfig effect(final PotionEffectType effectType, final Integer duration, final Integer amplifier) {
        return addConfig(new effect(effectType, duration, amplifier));
    }

    public void apply(final ItemStack itemStack) {
        for(final VirtualItemConfig config : configs) {
            config.apply(itemStack);
        }
    }
}
