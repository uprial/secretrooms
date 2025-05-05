package com.gmail.uprial.railnet.firework;

import org.bukkit.Color;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class FireworkMagicColorTest {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testMaxTypeAndAmount() throws Exception {
        final Color color = FireworkMagicColor.encode(new FireworkMagic(84, 767));
        final FireworkMagic fmc = FireworkMagicColor.decode(color);

        assertEquals(fmc.getType(), 84);
        assertEquals(fmc.getAmount(), 767);
    }

    @Test
    public void testTypeTooBig() throws Exception {
        e.expect(FireworkError.class);
        e.expectMessage("Type too big: 85");
        FireworkMagicColor.encode(new FireworkMagic(85, 767));
    }

    @Test
    public void testAmountTooBig() throws Exception {
        e.expect(FireworkError.class);
        e.expectMessage("Amount too big: 768");
        FireworkMagicColor.encode(new FireworkMagic(84, 768));
    }

    @Test
    public void testIdempotency() {
        for(int type = 0; type < 85; type++) {
            for(int amount = 0; amount < 256 * 3; amount++) {
                final Color color = FireworkMagicColor.encode(new FireworkMagic(type, amount));
                final FireworkMagic fmc = FireworkMagicColor.decode(color);

                assertEquals(type, fmc.getType());
                assertEquals(amount, fmc.getAmount());
            }
        }
    }
}