package com.gmail.uprial.secretrooms.populator.whirlpool;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.populator.*;
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

import static com.gmail.uprial.secretrooms.common.Formatter.format;

public class WhirlpoolPopulator extends AbstractSeedSpecificPopulator {
    private final CustomLogger customLogger;

    private static final double DEPTH_2_DENSITY = 20.0d;

    VirtualChunk vc;

    /*
        ==== Test ====

            $ grep "Whirlpool.*\] populated" logs/latest.log | wc -l
            317
     */
    private final static int PROBABILITY = 500;

    public WhirlpoolPopulator(final CustomLogger customLogger) {
        super(PROBABILITY);

        this.customLogger = customLogger;
    }

    public String getName() {
        return "Whirlpool";
    }

    private final ItemConfig fishingRodItemConfig = new ItemConfig()
            // Survival maximum level is 3, here it's 5
            .ench(Enchantment.LUCK_OF_THE_SEA, 0, 5)
            // Survival maximum level is 3, here it's 5
            .ench(Enchantment.LURE, 0, 5)
            // Survival maximum level is 3, here it's 5
            .ench(Enchantment.UNBREAKING, 0, 5);

    /*
        Ideated from:
            https://minecraft.wiki/w/Fishing
     */
    private final Map<Material, CLT> chestLootTable = ImmutableMap.<Material, CLT>builder()
            // 51%/2=25.5% for up to 2^(6-2)=16
            .put(Material.COD, new CLT(25.5D, CLT.MAX_POWER - 2))
            // 21.3%/2=10.65% for up to 16
            .put(Material.SALMON, new CLT(10.65D, CLT.MAX_POWER - 2))

            // 11.1% for 1-4
            .put(Material.PUFFERFISH, new CLT(11.1D, 2))
            // 1.7% for 1-4
            .put(Material.TROPICAL_FISH, new CLT(1.7D, 2))

            // 0.8%*2=1.6% for 1
            .put(Material.FISHING_ROD, new CLT(1.6D, fishingRodItemConfig))
            .put(Material.NAME_TAG, new CLT(1.6D))
            .put(Material.NAUTILUS_SHELL, new CLT(1.6D))
            .put(Material.SADDLE, new CLT(1.6D))
            .build();

    @Override
    protected void populateAppropriateChunk(final Chunk chunk) {
        vc = new VirtualChunk(getName(), chunk, BlockFace.NORTH);

        int minX = 1;
        int minY = vc.getSeaLevel();
        int minZ = 1;
        for(int x = 1; x < 15; x++) {
            for(int z = 1; z < 15; z++) {
                int y = vc.getSeaLevel();
                // +2 layers for a chest and a magma block under it
                while((y > vc.getMinHeight() + 2) && (isWaterLayer(x, y, z))) {
                    y--;
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
                customLogger.debug(String.format("%s[%s] can't be populated",
                        getName(), format(chunk)));
            }
            return ;
        }

        for(int dx = -1; dx <= 1; dx++) {
            for(int dz = -1; dz <= 1; dz++) {
                int y = minY;
                // +1 layer for a chest
                while((y > vc.getMinHeight() + 1) && isWater(minX + dx, y,  minZ + dz)) {
                    y--;
                }

                if(y < vc.getSeaLevel()) {
                    if (vc.get(minX + dx, y, minZ + dz).getType().equals(Material.MAGMA_BLOCK)) {
                        if (customLogger.isDebugMode()) {
                            // Idempotency marker
                            customLogger.debug(String.format("%s[%s] is already populated",
                                    getName(), format(chunk)));
                        }
                        return ;
                    }

                    vc.applyPhysicsOnce();
                    vc.set(minX + dx, y, minZ + dz, Material.MAGMA_BLOCK);

                    if ((dx == 0) && (dz == 0)) {
                        final Block block = vc.set(minX + dx, y - 1, minZ + dz, Material.CHEST);

                        final Inventory inventory = ((Chest) block.getState()).getBlockInventory();

                        final BlockSeed bs = BlockSeed.valueOf(block);
                        final ContentSeed cs = ContentSeed.valueOf(block);

                        // The deepest water the more loot in the chest.
                        int density = (int)Math.floor((vc.getSeaLevel() - y) / DEPTH_2_DENSITY);

                        int i = 0;

                        long callId = 0;
                        for(Map.Entry<Material, CLT> entry : chestLootTable.entrySet()) {
                            callId++;
                            if(entry.getValue().pass(callId, bs, density, chunk.getWorld().getName())) {
                                final int amount = entry.getValue().getRandomAmount(cs);

                                inventory.setItem(i, new ItemStack(entry.getKey(), amount));

                                // The fresh getItem() is needed to properly update the amount
                                entry.getValue().applyItemConfig(cs, inventory.getItem(i));

                                if (customLogger.isDebugMode()) {
                                    customLogger.debug(String.format("%s item #%d %s set to %d",
                                            format(block), i, entry.getKey(), amount));
                                }
                                i++;
                            }
                        }

                        // Will be analyzed in MineshaftPopulator to increase density.
                        vc.set(minX + dx, y - 2, minZ + dz, Material.MAGMA_BLOCK);
                    }
                }
            }
        }

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("%s[%s] populated", getName(), format(chunk)));
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
}