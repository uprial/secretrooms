package com.gmail.uprial.railnet.common;

import com.google.common.hash.Hashing;

public class HashUtils {
    public static long getHash(final long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }
}
