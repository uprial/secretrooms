package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.RandomUtils;
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

    private final List<VirtualItemConfig> configs = new ArrayList<>();

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
            itemStack.addUnsafeEnchantment(enchantment, RANDOM.nextInt(level1, level2 + 1));
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
        final Map<PotionEffectType,Integer> effectTypeOptions;
        final Set<Integer> durationOptions;

        effects(final Set<Integer> durationOptions, final Map<PotionEffectType,Integer> effectTypeOptions) {
            this.durationOptions = durationOptions;
            this.effectTypeOptions = effectTypeOptions;
        }

        @Override
        public void apply(final ItemStack itemStack) {
            final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

            final PotionEffectType effectType = RandomUtils.getSetItem(effectTypeOptions.keySet());

            final int amplifier = effectTypeOptions.get(effectType);

            int duration = RandomUtils.getSetItem(durationOptions);
            if(itemStack.getType().equals(Material.TIPPED_ARROW)) {
                /*
                    According to https://minecraft.wiki/w/Tipped_Arrow,
                    The duration of the effect is 1⁄8 that of the corresponding potion.
                 */
                duration *= 8;
            }

            potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            itemStack.setItemMeta(potionMeta);
        }
    }

    public ItemConfig() {
    }

    public ItemConfig ench(final Enchantment enchantment) {
        configs.add(new ench1(enchantment, 1));
        return this;
    }

    public ItemConfig ench(final Enchantment enchantment, final int level1, final int level2) {
        configs.add(new ench2(enchantment, level1, level2));
        return this;
    }

    public ItemConfig trim(final TrimMaterial material, final TrimPattern pattern) {
        configs.add(new trim(material, pattern));
        return this;
    }

    public ItemConfig amplify(final int level) {
        configs.add(new amplifier(level));
        return this;
    }

    public ItemConfig effects(final Set<Integer> durationOptions, final Map<PotionEffectType,Integer> effectTypeOptions) {
        configs.add(new effects(durationOptions, effectTypeOptions));
        return this;
    }

    public void apply(final ItemStack itemStack) {
        for(final VirtualItemConfig config : configs) {
            config.apply(itemStack);
        }
    }
}
