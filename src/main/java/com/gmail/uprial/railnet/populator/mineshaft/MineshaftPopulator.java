package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.*;

public class MineshaftPopulator implements ChunkPopulator {
    private final CustomLogger customLogger;

    private final Random random = new Random();

    // 2 ^ 6 = 64
    private final int MAX_POWER = 6;

    public MineshaftPopulator(final RailNet plugin, final CustomLogger customLogger) {
        //this.plugin = plugin;
        this.customLogger = customLogger;
    }

    @Override
    public void populate(final Chunk chunk) {
        final int minY = chunk.getWorld().getMinHeight();
        final int maxY = chunk.getWorld().getMaxHeight();
        for(int y = minY; y < maxY; y++) {
            for(int x = 0; x < 16; x++) {
                for(int z = 0; z < 16; z++) {
                    populateBlock(chunk.getBlock(x, y, z));
                }
            }
        }

        for(final Entity entity : chunk.getEntities()) {
            if(entity instanceof StorageMinecart) {
                populateStorageMinecart((StorageMinecart)entity);
            }
        }
    }

    private interface BlockPopulator {
        void populate(final Block block);
    }

    private final Map<Material, BlockPopulator> blockPopulators = ImmutableMap.<Material, BlockPopulator>builder()
            .put(Material.CHEST, this::populateChest)
            .put(Material.FURNACE, this::populateFurnace)
            .put(Material.BLAST_FURNACE, this::populateFurnace)
            .build();

    public void populateBlock(final Block block) {
        final BlockPopulator blockPopulator = blockPopulators.get(block.getType());
        if(blockPopulator != null) {
            blockPopulator.populate(block);
        }
    }

    // Ideated from https://minecraft.wiki/w/Rarity
    // Material -> probability to drop
    private final Map<Material, Double> chestLootTable = ImmutableMap.<Material, Double>builder()
            .put(Material.IRON_INGOT, 50.0D)

            .put(Material.GOLD_INGOT, 33.0D)
            .put(Material.LAPIS_LAZULI, 33.0D)
            .put(Material.REDSTONE, 33.0D)

            .put(Material.ENDER_PEARL, 20.0D)
            .put(Material.TNT, 20.0D)

            .put(Material.DIAMOND, 10.0D)
            .put(Material.END_CRYSTAL, 10.0D)
            .put(Material.GOLDEN_APPLE, 10.0D)
            .put(Material.GOLDEN_CARROT, 10.0D)

            .put(Material.ENCHANTED_GOLDEN_APPLE, 5.0D)
            .put(Material.NETHERITE_SCRAP, 5.0D)
            .put(Material.TOTEM_OF_UNDYING, 5.0D)
            .put(Material.WITHER_SKELETON_SKULL, 5.0D)

            .put(Material.NETHERITE_HELMET, 2.0D)
            .put(Material.NETHERITE_CHESTPLATE, 2.0D)
            .put(Material.NETHERITE_LEGGINGS, 2.0D)
            .put(Material.NETHERITE_BOOTS, 2.0D)

            .put(Material.SKELETON_SKULL, 1.0D)
            .put(Material.CREEPER_HEAD, 1.0D)
            .put(Material.PIGLIN_HEAD, 1.0D)
            .put(Material.PLAYER_HEAD, 1.0D)
            .put(Material.ZOMBIE_HEAD, 1.0D)

            .build();

    private final Set<Material> cloths = ImmutableSet.<Material>builder()
            .add(Material.NETHERITE_HELMET)
            .add(Material.NETHERITE_CHESTPLATE)
            .add(Material.NETHERITE_LEGGINGS)
            .add(Material.NETHERITE_BOOTS)
            .build();

    private void stackInventoryOnce(final String title, final Inventory inventory) {
        final Map<Material,Integer> map = new HashMap<>();

        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if(itemStack != null) {
                if(map.containsKey(itemStack.getType())) {
                    final int existingI = map.get(itemStack.getType());
                    final ItemStack existingItemStack = inventory.getItem(existingI);
                    if(existingItemStack.getAmount() < existingItemStack.getMaxStackSize()) {
                        final int diff = Math.min(
                                existingItemStack.getMaxStackSize() - existingItemStack.getAmount(),
                                itemStack.getAmount()
                        );

                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s merged: %d %s from item #%d moved to item #%d",
                                    title, diff, itemStack.getType(), i, existingI));
                        }

                        existingItemStack.setAmount(existingItemStack.getAmount() + diff);
                        if(itemStack.getAmount() > diff) {
                            itemStack.setAmount(itemStack.getAmount() - diff);
                        } else {
                            inventory.setItem(i, null);
                            return;
                        }
                    }
                }

                map.put(itemStack.getType(), i);
            }
        }

        if (customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s doesn't have space", title));
        }
    }

    private void populateInventory(final String title, final Inventory inventory) {
        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */
        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if((itemStack != null) && (itemStack.getMaxStackSize() > 1) && (pass(10.0D))) {
                setAmount(String.format("%s item #%d", title, i),
                        itemStack.getAmount(), itemStack, 1, MAX_POWER);
            }
        }

        for(Map.Entry<Material, Double> entry : chestLootTable.entrySet()) {
            if(pass(entry.getValue())) {
                int i = inventory.firstEmpty();
                if(i == -1) {
                    // There are no empty slots.
                    stackInventoryOnce(title, inventory);
                    i = inventory.firstEmpty();
                    if(i == -1) {
                        /*
                            There are no empty slots even after the stack.
                            Potentially, other random items may be added, but it isn't interesting.
                         */
                        break;
                    }
                }
                inventory.setItem(i, new ItemStack(entry.getKey(), 1));

                if(cloths.contains(entry.getKey())) {
                    final ItemStack itemStack = inventory.getItem(i);
                    itemStack.addUnsafeEnchantment(Enchantment.PROTECTION, 4);

                    final ArmorMeta armorMeta = (ArmorMeta)itemStack.getItemMeta();
                    armorMeta.setTrim(new ArmorTrim(TrimMaterial.NETHERITE, TrimPattern.RIB));
                    itemStack.setItemMeta(armorMeta);
                }

                setAmount(String.format("%s item #%d", title, i),
                        // The fresh getItem() is needed to properly update the amount
                        0, inventory.getItem(i), 0, (int)Math.round(entry.getValue() / 10.0D));
            }
        }
    }

    private void populateChest(final Block block) {
        populateInventory(format(block), ((Chest)block.getState()).getBlockInventory());

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated", format(block)));
        }
    }

    private void populateStorageMinecart(final StorageMinecart storageMinecart) {
        populateInventory(format(storageMinecart), storageMinecart.getInventory());

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s populated", format(storageMinecart)));
        }
    }

    // Ideated from https://minecraft.wiki/w/Smelting
    // Material -> max power of drop
    private final Map<Material,Integer> furnaceResultTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.COPPER_INGOT, MAX_POWER)
            .put(Material.COOKED_BEEF, MAX_POWER - 1)
            .put(Material.IRON_INGOT, MAX_POWER - 2)
            .put(Material.GOLD_INGOT, MAX_POWER - 3)
            .build();

    private final Map<Material,Integer> furnaceFuelTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.COAL, MAX_POWER)
            .put(Material.COAL_BLOCK, MAX_POWER - 2)
            .put(Material.LAVA_BUCKET, 0)
            .build();


    private interface ItemStackGetter {
        ItemStack get();
    }

    private interface ItemStackSetter {
        void set(final ItemStack itemStack);
    }

    private void updateItemStack(final String title,
                                 final ItemStackGetter itemStackGetter,
                                 final ItemStackSetter itemStackSetter,
                                 final Map<Material,Integer> lootTable) {
        ItemStack itemStack = itemStackGetter.get();
        final int oldAmount;
        final int maxPower;
        if (itemStack == null) {
            oldAmount = 0;
            final Material material = getRandomSetItem(lootTable.keySet());
            itemStackSetter.set(new ItemStack(material, 1));
            // The sequence is needed to properly update the amount
            itemStack = itemStackGetter.get();
            maxPower = lootTable.get(material);
        } else {
            oldAmount = itemStack.getAmount();
            maxPower = MAX_POWER - 1;
        }

        setAmount(title, oldAmount, itemStack, 0, maxPower);
    }

    private void populateFurnace(final Block block) {
        final Furnace furnace = (Furnace)block.getState();

        final FurnaceInventory inventory = furnace.getInventory();

        updateItemStack(String.format("%s fuel", format(block)),
                inventory::getFuel,
                inventory::setFuel,
                furnaceFuelTable);

        updateItemStack(String.format("%s result", format(block)),
                inventory::getResult,
                inventory::setResult,
                furnaceResultTable);
    }

    private String format(final Block block) {
        return String.format("%s[%s:%d:%d:%d]",
                block.getType(),
                block.getWorld().getName(),
                block.getX(), block.getY(), block.getZ());
    }

    private String format(final Entity entity) {
        return String.format("%s[%s:%.0f:%.0f:%.0f]",
                entity.getType(),
                entity.getWorld().getName(),
                entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
    }

    private void setAmount(final String title, final int oldAmount, final ItemStack itemStack,
                           final int minPower, final int maxPower) {
        final int newAmount =
                Math.min(
                        itemStack.getMaxStackSize(),
                        itemStack.getAmount() * (int)Math.pow(2.0, random.nextInt(minPower, maxPower + 1))
                );

        itemStack.setAmount(newAmount);

        if (customLogger.isDebugMode()) {
            if(oldAmount == 0) {
                customLogger.debug(String.format("%s %s set to %d",
                        title, itemStack.getType(), newAmount));
            } else if(newAmount > oldAmount) {
                customLogger.debug(String.format("%s %s updated from %d to %d",
                        title, itemStack.getType(), oldAmount, newAmount));
            } else {
                customLogger.warning(String.format("%s %s kept as %d",
                        title, itemStack.getType(), newAmount));
            }
        }
    }

    private boolean pass(final double probability) {
        return (random.nextDouble() * 100.0D) < probability;
    }

    private <T> T getRandomSetItem(final Set<T> set) {
        return  (new ArrayList<>(set)).get(random.nextInt(set.size()));
    }
}
