package com.gmail.uprial.railnet.common;

import java.util.Random;

public class Probability {
    private final static Random RANDOM = new Random();

    private final static double MAX_PERCENT = 100.0D;

    public static boolean PASS(final double probability, final int density) {
        return (RANDOM.nextDouble() * MAX_PERCENT) < (probability * (1.0D + density));
    }
}

