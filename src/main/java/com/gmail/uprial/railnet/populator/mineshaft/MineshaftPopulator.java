package com.gmail.uprial.railnet.populator.mineshaft;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiFunction;

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
    private final Map<Material, Integer> chestLootTable = ImmutableMap.<Material, Integer>builder()
            .put(Material.IRON_INGOT, 50)

            .put(Material.GOLD_INGOT, 33)
            .put(Material.LAPIS_LAZULI, 33)
            .put(Material.REDSTONE, 33)

            .put(Material.EMERALD, 20)
            .put(Material.DIAMOND, 20)

            .put(Material.GOLDEN_APPLE, 10)
            .put(Material.EXPERIENCE_BOTTLE, 10)

            .put(Material.TOTEM_OF_UNDYING, 5)
            .put(Material.ENCHANTED_GOLDEN_APPLE, 5)

            .put(Material.WITHER_SKELETON_SKULL, 3)
            .put(Material.NETHERITE_SCRAP, 3)

            .put(Material.PLAYER_HEAD, 1)
            .put(Material.ZOMBIE_HEAD, 1)
            .put(Material.CREEPER_HEAD, 1)
            .put(Material.SKELETON_SKULL, 1)
            .put(Material.PIGLIN_HEAD, 1)

            .build();

    private void populateChest(final Block block) {
        final Chest chest = (Chest)block.getState();

        /*
            getContents() returns a list of nulls
            even when the content isn't actually null,
            so I iterate the content by id.
         */
        final Inventory inventory = chest.getBlockInventory();

        final BiFunction<Block,Integer,String> format
                = (final Block b, final Integer i)
                -> String.format("%s %s item #%d", b.getType(), format(b), i);

        for(int i = 0; i < inventory.getSize(); i++) {
            final ItemStack itemStack = inventory.getItem(i);
            if((itemStack != null) && (pass(10))) {
                setAmount(format.apply(block, i), itemStack.getAmount(), itemStack, MAX_POWER);
            }
        }

        for(Map.Entry<Material, Integer> entry : chestLootTable.entrySet()) {
            if(pass(entry.getValue())) {
                final int i = inventory.firstEmpty();
                if(i == -1) {
                    // no empty slots
                    break;
                }
                inventory.setItem(i, new ItemStack(entry.getKey(), 1));

                setAmount(format.apply(block, i),
                        // The sequence is needed to properly update the amount
                        0, inventory.getItem(i), entry.getValue() / 10);
            }
        }

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("Chest %s populated", format(block)));
        }
    }

    // Ideated from https://minecraft.wiki/w/Smelting
    // Material -> max power of drop
    private final Map<Material,Integer> furnaceLootTable = ImmutableMap.<Material,Integer>builder()
            .put(Material.COPPER_INGOT, MAX_POWER)
            .put(Material.COOKED_BEEF, MAX_POWER - 1)
            .put(Material.IRON_INGOT, MAX_POWER - 2)
            .put(Material.GOLD_INGOT, MAX_POWER - 3)
            .build();

    private void populateFurnace(final Block block) {
        final Furnace furnace = (Furnace)block.getState();

        final FurnaceInventory inventory = furnace.getInventory();

        {
            ItemStack fuel = inventory.getFuel();
            final int oldAmount;
            if (fuel == null) {
                oldAmount = 0;
                inventory.setFuel(new ItemStack(Material.COAL, 1));
                // The sequence is needed to properly update the amount
                fuel = inventory.getFuel();
            } else {
                oldAmount = fuel.getAmount();
            }

            setAmount(String.format("Furnace %s fuel", format(block)),
                    oldAmount, fuel, MAX_POWER);
        }

        {
            ItemStack result = inventory.getResult();
            final int oldAmount;
            final int maxPower;
            if (result == null) {
                oldAmount = 0;
                final Material material = (new ArrayList<>(furnaceLootTable.keySet()))
                        .get(random.nextInt(furnaceLootTable.size()));
                inventory.setResult(new ItemStack(material, 1));
                // The sequence is needed to properly update the amount
                result = inventory.getResult();
                maxPower = furnaceLootTable.get(material);
            } else {
                oldAmount = result.getAmount();
                maxPower = MAX_POWER - 1;
            }

            setAmount(String.format("Furnace %s result", format(block)),
                    oldAmount, result, maxPower);

        }
    }

    private String format(final Block block) {
        return String.format("%s:%d:%d:%d",
                block.getWorld().getName(),
                block.getX(), block.getY(), block.getZ());
    }

    private void setAmount(final String title, final int oldAmount, final ItemStack itemStack, final int maxPower) {
        final int newAmount =
                Math.min(
                        itemStack.getMaxStackSize(),
                        itemStack.getAmount() * (int)Math.pow(2.0, random.nextInt(maxPower + 1))
                );

        if(newAmount > itemStack.getAmount()) {
            itemStack.setAmount(newAmount);

            if (customLogger.isDebugMode()) {
                if(oldAmount == 0) {
                    customLogger.debug(String.format("%s %s set to %d",
                            title, itemStack.getType(), newAmount));
                } else {
                    customLogger.debug(String.format("%s %s updated from %d to %d",
                            title, itemStack.getType(), oldAmount, newAmount));
                }
            }
        }
    }

    private boolean pass(final int probability) {
        return (random.nextInt(100) < probability);
    }
}
