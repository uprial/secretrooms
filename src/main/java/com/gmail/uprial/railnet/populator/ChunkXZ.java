package com.gmail.uprial.railnet.populator;

public class ChunkXZ {
    final private int x;
    final private int z;

    public ChunkXZ(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ChunkXZ getSum(final ChunkXZ chunkXZ) {
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
        return String.format("%d:%d", x, z);
    }
}