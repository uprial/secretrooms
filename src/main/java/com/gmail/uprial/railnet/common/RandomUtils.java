package com.gmail.uprial.railnet.common;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class RandomUtils {
    private final static Random RANDOM = new Random();

    public static <T> T getSetItem(final Set<T> set) {
        return  (new ArrayList<>(set)).get(RANDOM.nextInt(set.size()));
    }
}