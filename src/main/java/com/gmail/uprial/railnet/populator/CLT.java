package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.common.WorldName;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    private final ItemConfig itemConfig;
    private final Set<String> worldNames = new HashSet<>();

    public CLT(final double probability) {
        this.probability = probability;
        this.maxPower = 0;
        this.itemConfig = null;
    }

    public CLT(final double probability, final int maxPower) {
        this.probability = probability;
        this.maxPower = maxPower;
        this.itemConfig = null;
    }

    public CLT(final double probability, final ItemConfig itemConfig) {
        this.probability = probability;
        this.maxPower = 0;
        this.itemConfig = itemConfig;
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

    public ItemConfig getItemConfig() {
        return itemConfig;
    }

    public CLT onlyInWorld(final String worldName) {
        worldNames.add(WorldName.normalize(worldName));
        return this;
    }

    public boolean isAppropriateWorld(final String worldName) {
        return worldNames.isEmpty() || worldNames.contains(WorldName.normalize(worldName));
    }
}