package com.gmail.uprial.railnet.populator.railway.map;

public enum RailType {
    SURFACE(1),
    UNDERGROUND(2);

    final private int hashCode;
    RailType(final int hashCode) {
        this.hashCode = hashCode;
    }

    int getHashCode() {
        return hashCode;
    }

    int getMaxHashCode() {
        return 2;
    }
}