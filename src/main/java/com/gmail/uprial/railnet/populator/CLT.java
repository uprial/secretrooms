package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.WorldName;

import java.util.*;

// ChestLootConfig
public class CLT {
    private final static Random RANDOM = new Random();

    // 2 ^ 6 = 64
    public final static int MAX_POWER = 6;

    public static int getRandomAmount(final int minPower, final int maxPower) {
        return (int)Math.pow(2.0, RANDOM.nextInt(minPower, maxPower + 1));
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

    public double getProbability() {
        return probability;
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

    public boolean hasItemConfig() {
        return !itemConfigOptions.isEmpty();
    }

    public ItemConfig getItemConfig() {
        return itemConfigOptions.get(RANDOM.nextInt(itemConfigOptions.size()));
    }

    public CLT onlyInWorld(final String worldName) {
        worldNames.add(WorldName.normalize(worldName));
        return this;
    }

    public boolean isAppropriateWorld(final String worldName) {
        return worldNames.isEmpty() || worldNames.contains(WorldName.normalize(worldName));
    }
}