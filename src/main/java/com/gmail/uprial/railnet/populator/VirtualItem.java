package com.gmail.uprial.railnet.populator;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.Random;

public class VirtualItem {
    private final ItemStack itemStack;

    private static final Random random = new Random();

    public VirtualItem(final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public VirtualItem ench(final Enchantment enchantment, final int level1, final int level2) {
        return ench(enchantment, random.nextInt(level1, level2 + 1));
    }

    public VirtualItem ench(final Enchantment enchantment, final int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);

        return this;
    }

    public VirtualItem trim(final TrimMaterial material, final TrimPattern pattern) {
        final ArmorMeta armorMeta = (ArmorMeta)itemStack.getItemMeta();
        armorMeta.setTrim(new ArmorTrim(material, pattern));
        itemStack.setItemMeta(armorMeta);

        return this;
    }
}
