package com.gmail.uprial.secretrooms.populator;

import org.bukkit.block.Block;

public class DistanceDensity {
    private final int multiplier;

    public DistanceDensity(final int multiplier) {
        this.multiplier = multiplier;
    }

    public int get(final Block block) {
        if(multiplier > 0) {
            return (int) Math.floor(
                    Math.sqrt(Math.pow(block.getX(), 2.0D) + Math.pow(block.getZ(), 2.0D))
                            / multiplier
            );
        } else {
            return 0;
        }
    }
}
