package com.gmail.uprial.railnet.populator;

import java.util.HashSet;
import java.util.Set;

public class PopulationHistory {

    private final Set<String> data = new HashSet<>();

    public void add(final String item) {
        data.add(item);
    }

    public boolean contains(final String item) {
        return data.contains(item);
    }
}
