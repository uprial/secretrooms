package com.gmail.uprial.railnet.populator.whirlpool;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.*;
import com.gmail.uprial.railnet.populator.mineshaft.MineshaftPopulator;
import com.gmail.uprial.railnet.populator.railway.RailWayPopulator;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class WhirlpoolPopulator extends AbstractSeedSpecificPopulator {
    private final RailNet plugin;
    private final CustomLogger customLogger;

    private static final double DEPTH_2_DENSITY = 20.0d;

    VirtualChunk vc;

    final static String WORLD = WorldName.WORLD;
    final static int DENSITY = 100;

    public WhirlpoolPopulator(final RailNet plugin, final CustomLogger customLogger) {
        super(WORLD, DENSITY);

        this.plugin = plugin;
        this.customLogger = customLogger;
    }

    private final ItemConfig fishingRodItemConfig = new ItemConfig()
            .ench(Enchantment.LUCK_OF_THE_SEA, 0, 3)
            .ench(Enchantment.LURE, 0, 3)
            .ench(Enchantment.UNBREAKING, 0, 3);

    /*
        Ideated from:
            https://minecraft.wiki/w/Fishing
     */
    private final Map<Material, CLT> chestLootTable = ImmutableMap.<Material, CLT>builder()
            // 60%/2=30% for up to 2^(6-2)=16
            .put(Material.COD, new CLT(30.0D, CLT.MAX_POWER - 2))
            // 25%/2=12.5% for up to 16
            .put(Material.SALMON, new CLT(12.5D, CLT.MAX_POWER - 2))

            // 13% for 1-4
            .put(Material.PUFFERFISH, new CLT(13.0D, 2))
            // 2*2=4% for 1-4
            .put(Material.TROPICAL_FISH, new CLT(4.0D, 2))

            .put(Material.FISHING_ROD, new CLT(10.0D, fishingRodItemConfig))
            .build();

    private static final double MINESHAFT_POPULATION_PROBABILITY = 33.0D;

    protected void populateAppropriateChunk(final Chunk chunk) {
        vc = new VirtualChunk("Whirlpool", chunk, BlockFace.NORTH);

        int minX = 1;
        int minY = vc.getSeaLevel();
        int minZ = 1;
        for(int x = 1; x < 15; x++) {
            for(int z = 1; z < 15; z++) {
                int y = vc.getSeaLevel();
                // +1 layer for a chest
                while((y > vc.getMinHeight() + 1) && (isWaterLayer(x, y, z))) {
                    y--;
                }
                // Don't overlap with other structures
                while((y < vc.getSeaLevel()) && (isConflicting(x, y, z))) {
                    y++;
                }
                if(y < minY) {
                    minX = x;
                    minY = y;
                    minZ = z;
                }
            }
        }

        if(minY >= vc.getSeaLevel()) {
            if(customLogger.isDebugMode()) {
                // No water
                customLogger.debug(String.format("Whirlpool[%s] can't be populated", format(chunk)));
            }
            return;
        }

        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                int y = minY;
                // +1 layer for a chest
                while((y > vc.getMinHeight() + 1) && isWater(minX + dx, y,  minZ + dz)) {
                    y--;
                }
                // Don't overlap with other structures
                while((y < vc.getSeaLevel()) && (isConflicting(minX + dx, y,  minZ + dz))) {
                    y++;
                }

                if(y < vc.getSeaLevel()) {
                    if (vc.get(minX + dx, y, minZ + dz).getType().equals(Material.MAGMA_BLOCK)) {
                        if (customLogger.isDebugMode()) {
                            // Idempotency marker
                            customLogger.debug(String.format("Whirlpool[%s] is already populated", format(chunk)));
                        }
                        return;
                    }

                    vc.applyPhysicsOnce();
                    vc.set(minX + dx, y, minZ + dz, Material.MAGMA_BLOCK);

                    if ((dx == 0) && (dz == 0)
                            // Sacrifice a chest when overlaps with other structures
                            && (!isConflicting(minX + dx, y - 1, minZ + dz))) {
                        final Block block = vc.set(minX + dx, y - 1, minZ + dz, Material.CHEST);

                        final Inventory inventory = ((Chest) block.getState()).getBlockInventory();

                        // The deepest water the more loot in the chest.
                        int density = (int)Math.floor((vc.getSeaLevel() - y) / DEPTH_2_DENSITY);

                        int i = 0;

                        for(Map.Entry<Material, CLT> entry : chestLootTable.entrySet()) {
                            if(entry.getValue().pass(density, chunk.getWorld().getName())) {
                                final int amount = entry.getValue().getRandomAmount();

                                inventory.setItem(i, new ItemStack(entry.getKey(), amount));

                                // The fresh getItem() is needed to properly update the amount
                                entry.getValue().applyItemConfig(inventory.getItem(i));

                                if (customLogger.isDebugMode()) {
                                    customLogger.debug(String.format("%s item #%d %s set to %d",
                                            format(block), i, entry.getKey(), amount));
                                }
                                i++;
                            }
                        }

                        if(Probability.PASS(MINESHAFT_POPULATION_PROBABILITY, 0)) {
                            // One more population
                            new MineshaftPopulator(plugin, customLogger).populateChest(block, density);
                        }
                    }
                }
            }
        }

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("Whirlpool[%s] populated", format(chunk)));
        }
    }

    private boolean isWater(final int x, final int y, final int z) {
        return vc.get(x, y, z).getType().equals(Material.WATER);
    }

    private boolean isWaterLayer(final int x, final int y, final int z) {
        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                if(!isWater(x + dx, y, z + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isConflicting(final int x, final int y, final int z) {
        return RailWayPopulator.isBorderBlock(vc.get(x, y, z).getType());
    }
}