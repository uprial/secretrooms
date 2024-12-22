package com.gmail.uprial.railnet.firework;

public class FireworkMagic {
    private final int type;
    private final int amount;

    FireworkMagic(final int type, final int amount) {
        this.type = type;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public int getType() {
        return type;
    }
}
