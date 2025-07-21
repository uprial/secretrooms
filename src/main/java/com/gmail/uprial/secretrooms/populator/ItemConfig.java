package com.gmail.uprial.secretrooms.populator;

import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.gmail.uprial.secretrooms.common.Utils.getFormattedTicks;
import static com.gmail.uprial.secretrooms.common.Utils.joinStrings;

public class ItemConfig {
    private interface VirtualItemConfig {
        void apply(final ContentSeed cs, final ItemStack itemStack);
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
            itemStack.addUnsafeEnchantment(enchantment, level1 + (int)cs.oneOf(level2 - level1 + 1));
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
            final PotionEffectType effectType = cs.oneOf(effectTypeOptions.keySet());

            final int amplifier = effectTypeOptions.get(effectType);

            int duration = cs.oneOf(durationOptions);
            if (itemStack.getType().equals(Material.TIPPED_ARROW)) {
                /*
                    According to https://minecraft.wiki/w/Tipped_Arrow,
                    The duration of the effect is 1⁄8 that of the corresponding potion.
                 */
                duration *= 8;
            }

            new effect(effectType, duration, amplifier).apply(cs, itemStack);
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
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
            final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();

            potionMeta.addCustomEffect(new PotionEffect(effectType, duration, amplifier), true);
            itemStack.setItemMeta(potionMeta);
        }
    }

    private static abstract class itemmeta<T> implements VirtualItemConfig {
        final T value;

        itemmeta(final T value) {
            this.value = value;
        }

        abstract void setmeta(final ItemMeta itemMeta, final T value);

        @Override
        public void apply(final ContentSeed cs, final ItemStack itemStack) {
            final ItemMeta itemMeta = itemStack.getItemMeta();
            setmeta(itemMeta, value);
            itemStack.setItemMeta(itemMeta);
        }
    }

    private static class glider extends itemmeta<Boolean> {
        glider(final Boolean value) {
            super(value);
        }

        @Override
        void setmeta(final ItemMeta itemMeta, final Boolean value) {
            itemMeta.setGlider(value);
        }
    }

    private static class rarity extends itemmeta<ItemRarity> {
        rarity(final ItemRarity value) {
            super(value);
        }

        @Override
        void setmeta(final ItemMeta itemMeta, final ItemRarity value) {
            itemMeta.setRarity(value);
        }
    }

    private static class lore extends itemmeta<List<String>> {
        lore(final List<String> value) {
            super(value);
        }

        @Override
        void setmeta(final ItemMeta itemMeta, final List<String> value) {
            itemMeta.setLore(value);
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

    public ItemConfig effect(final PotionEffectType effectType, final Integer duration, final Integer amplifier) {
        return addConfig(new effect(effectType, duration, amplifier));
    }

    public ItemConfig glider(final Boolean value) {
        return addConfig(new glider(value));
    }

    public ItemConfig rarity(final ItemRarity value) {
        return addConfig(new rarity(value));
    }

    public ItemConfig lore(final List<String> value) {
        return addConfig(new lore(value));
    }

    public void apply(final ContentSeed cs, final ItemStack itemStack) {
        for(final VirtualItemConfig config : configs) {
            config.apply(cs, itemStack);
        }
    }


    public static String format(final ItemStack itemStack) {
        final StringBuilder sb = new StringBuilder();
        sb.append(itemStack.getType());

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta instanceof PotionMeta) {
            final List<String> contents = new ArrayList<>();
            final PotionMeta potionMeta = (PotionMeta)itemMeta;
            for(final PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                contents.add(String.format("%s-%d-%s",
                        potionEffect.getType().getName(),
                        potionEffect.getAmplifier(),
                        getFormattedTicks(potionEffect.getDuration())));
            }
            sb.append("[").append(joinStrings(",", contents)).append("]");
        }

        if(!itemStack.getEnchantments().isEmpty()) {
            final List<String> contents = new ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
                contents.add(String.format("%s-%d",
                        entry.getKey().getName(), entry.getValue()));
            }
            sb.append("[").append(joinStrings(",", contents)).append("]");
        }

        return sb.toString();
    }
}
