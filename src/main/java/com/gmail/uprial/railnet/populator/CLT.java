package com.gmail.uprial.railnet.populator;

import java.util.Random;

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
}