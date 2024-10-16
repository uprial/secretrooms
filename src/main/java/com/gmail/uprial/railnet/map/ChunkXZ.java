package com.gmail.uprial.railnet.map;

public class ChunkXZ {
    final private int x;
    final private int z;

    public ChunkXZ(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    int getX() {
        return x;
    }

    int getZ() {
        return z;
    }

    ChunkXZ getSum(final ChunkXZ chunkXZ) {
        return new ChunkXZ(this.x + chunkXZ.x, this.z + chunkXZ.z);
    }

    @Override
    public int hashCode() {
        return x * 1000 + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        final ChunkXZ chunkXZ = (ChunkXZ)o;
        return x == chunkXZ.x
                && z == chunkXZ.z;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", x, z);
    }
}