package com.gmail.uprial.secretrooms.populator;

import com.gmail.uprial.secretrooms.common.BlockSeed;
import org.bukkit.inventory.ItemStack;

import java.util.*;

// ChestLootTable
public class CLT {
    // 2 ^ 6 = 64
    public final static int MAX_POWER = 6;

    public static int getRandomAmount(final BlockSeed bs, final int minPower, final int maxPower) {
        return (int)Math.pow(2.0, minPower + bs.oneOf(maxPower - minPower + 1));
    }

    private final double probability;
    private final int maxPower;
    private final ItemConfig itemConfig;
    private final Set<String> worldNames = new HashSet<>();

    public CLT(final double probability) {
        this(probability, null, 0);
    }

    public CLT(final double probability, final int maxPower) {
        this(probability, null, maxPower);
    }

    public CLT(final double probability, final ItemConfig itemConfig) {
        this(probability, itemConfig, 0);
    }

    public CLT(final double probability, final ItemConfig itemConfig, final int maxPower) {
        this.probability = probability;
        this.itemConfig = itemConfig;
        this.maxPower = maxPower;
    }

    public boolean pass(final long callId, final BlockSeed bs, final int density, final String worldName) {
        return bs.pass(callId, probability, density)
                && (worldNames.isEmpty() || worldNames.contains(worldName));
    }

    public void applyItemConfig(final ContentSeed cs, final ItemStack itemStack) {
        if (itemConfig != null) {
            itemConfig.apply(cs, itemStack);
        }
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getRandomAmount(final BlockSeed bs) {
        return getRandomAmount(bs, 0, maxPower);
    }

    public CLT onlyInWorld(final String worldName) {
        worldNames.add(worldName);
        return this;
    }
}