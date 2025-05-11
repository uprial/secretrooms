package com.gmail.uprial.railnet.helpers;

import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionType;

public class TestUnsafeValues implements UnsafeValues {
    @Override
    public Material toLegacy(Material material) {
        return null;
    }

    @Override
    public Material fromLegacy(Material material) {
        return null;
    }

    @Override
    public Material fromLegacy(MaterialData material) {
        return null;
    }

    @Override
    public Material fromLegacy(MaterialData material, boolean itemPriority) {
        return null;
    }

    @Override
    public BlockData fromLegacy(Material material, byte data) {
        return null;
    }

    @Override
    public Material getMaterial(String material, int version) {
        return null;
    }

    @Override
    public int getDataVersion() {
        return 0;
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        return null;
    }

    @Override
    public void checkSupported(PluginDescriptionFile pdf) {

    }

    @Override
    public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
        return new byte[0];
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey key) {
        return false;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material material, EquipmentSlot slot) {
        return null;
    }

    @Override
    public CreativeCategory getCreativeCategory(Material material) {
        return null;
    }

    @Override
    public String getBlockTranslationKey(Material material) {
        return "";
    }

    @Override
    public String getItemTranslationKey(Material material) {
        return "";
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        return "";
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        return "";
    }

    @Override
    public String getTranslationKey(Attribute attribute) {
        return "";
    }

    @Override
    public FeatureFlag getFeatureFlag(NamespacedKey key) {
        return null;
    }

    @Override
    public PotionType.InternalPotionData getInternalPotionData(NamespacedKey key) {
        return null;
    }

    @Override
    public DamageEffect getDamageEffect(String key) {
        return null;
    }

    @Override
    public DamageSource.Builder createDamageSourceBuilder(DamageType damageType) {
        return null;
    }

    @Override
    public String get(Class<?> aClass, String value) {
        return "";
    }

    @Override
    public <B extends Keyed> B get(Registry<B> registry, NamespacedKey key) {
        return null;
    }

    @Override
    public Biome getCustomBiome() {
        return null;
    }

    @Override
    public Villager.ReputationType createReputationType(String key) {
        return null;
    }

    @Override
    public Villager.ReputationEvent createReputationEvent(String key) {
        return null;
    }
}
