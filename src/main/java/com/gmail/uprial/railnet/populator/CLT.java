package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.Probability;
import com.gmail.uprial.railnet.common.WorldName;
import org.bukkit.inventory.ItemStack;

import java.util.*;

// ChestLootTable
public class CLT {
    private final static Random RANDOM = new Random();

    // 2 ^ 6 = 64
    public final static int MAX_POWER = 6;

    public static int getRandomAmount(final int minPower, final int maxPower) {
        return (int)Math.pow(2.0, minPower + RANDOM.nextInt(maxPower - minPower + 1));
    }

    private final double probability;
    private final int maxPower;
    private final List<ItemConfig> itemConfigOptions = new ArrayList<>();
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
        if(itemConfig != null) {
            this.itemConfigOptions.add(itemConfig);
        }
        this.maxPower = maxPower;
    }

    public boolean pass(final int density, final String worldName) {
        return Probability.PASS(probability, density)
                && (worldNames.isEmpty() || worldNames.contains(WorldName.normalize(worldName)));
    }

    public void applyItemConfig(final ItemStack itemStack) {
        if (!itemConfigOptions.isEmpty()) {
            itemConfigOptions.get(RANDOM.nextInt(itemConfigOptions.size())).apply(itemStack);
        }
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getRandomAmount() {
        return getRandomAmount(0, maxPower);
    }

    public CLT addItemConfigOption(final ItemConfig itemConfig) {
        this.itemConfigOptions.add(itemConfig);
        return this;
    }

    public CLT onlyInWorld(final String worldName) {
        worldNames.add(WorldName.normalize(worldName));
        return this;
    }
}