package com.gmail.uprial.railnet.firework;

class FireworkMagic {
    private final int type;
    private final int amount;

    FireworkMagic(final int type, final int amount) {
        this.type = type;
        this.amount = amount;
    }

    int getAmount() {
        return amount;
    }

    int getType() {
        return type;
    }
}
