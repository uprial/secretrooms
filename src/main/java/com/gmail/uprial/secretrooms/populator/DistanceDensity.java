package com.gmail.uprial.secretrooms.populator;

import org.bukkit.block.Block;

public class DistanceDensity {
    private final int multiplier;

    public DistanceDensity(final int multiplier) {
        this.multiplier = multiplier;
    }

    private final static double POW_DOWN = 0.667D;
    public int get(final Block block, final int max) {
        if(multiplier > 0) {
            final double distance = Math.sqrt(
                    Math.pow(Math.abs(block.getX()), 2.0D)
                    + Math.pow(Math.abs(block.getZ()), 2.0D));

            return Math.min(max, (int) Math.floor(Math.pow(distance / multiplier, POW_DOWN)));
        } else {
            return 0;
        }
    }
}
