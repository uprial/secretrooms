package com.gmail.uprial.railnet.map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.bukkit.block.BlockFace;

import java.util.*;

public class ChunkMap {
    private final String title;

    static class OrdinalRailWay {
        final private int wayId;
        final private RailType railType;
        final private BlockFace blockFace;

        OrdinalRailWay(final int wayId, final RailType railType, final BlockFace blockFace) {
            this.wayId = wayId;
            this.railType = railType;
            this.blockFace = blockFace;
        }

        boolean isAnotherWayOfTheSameType(final OrdinalRailWay ordinalRailWay) {
            return (this.railType == ordinalRailWay.railType)
                    && (this.wayId != ordinalRailWay.wayId);
        }

        RailType getRailType() {
            return railType;
        }

        BlockFace getBlockFace() {
            return blockFace;
        }

        @Override
        public int hashCode() {
            return wayId * railType.getMaxHashCode() * 128 + railType.getHashCode() * 64 + blockFace.getModX() * 16 + blockFace.getModY() * 4 + blockFace.getModZ();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            if (this.getClass() != o.getClass()) return false;
            final OrdinalRailWay ordinalRailWay = (OrdinalRailWay)o;
            return wayId == ordinalRailWay.wayId
                    && railType == ordinalRailWay.railType
                    && blockFace == ordinalRailWay.blockFace;
        }

        @Override
        public String toString() {
            return String.format("%s-%s#%s", railType, blockFace, wayId);
        }
    }

    final private static class OrdinalRailWaySet extends LinkedHashSet<OrdinalRailWay> {

    }

    final private Map<ChunkXZ, OrdinalRailWaySet> map = new LinkedHashMap<>();

    public ChunkMap (final String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    int wayId = 0;

    private final List<ChunkXZ> overlapChecklist
            = ImmutableList.<ChunkXZ>builder()
            .add(new ChunkXZ(-1, 0))
            .add(new ChunkXZ(0, 0))
            .add(new ChunkXZ(+1, 0))
            .add(new ChunkXZ(0, -1))
            .add(new ChunkXZ(0, +1))
            .build();

    private void add(final int x, final int z, final RailType railType, final BlockFace blockFace) throws InvalidMapException {
        final ChunkXZ chunkXZ = new ChunkXZ(x, z);
        final OrdinalRailWay ordinalRailWay = new OrdinalRailWay(wayId, railType, blockFace);

        final OrdinalRailWaySet ordinalRailWaySet = map.computeIfAbsent(chunkXZ, k -> new OrdinalRailWaySet());

        for(final OrdinalRailWay _ordinalRailWay : ordinalRailWaySet) {
            if(_ordinalRailWay.getRailType() == ordinalRailWay.getRailType()) {
                throw new InvalidMapException(
                        String.format("Chunk %s already contains the same type %s in %s", chunkXZ, ordinalRailWay.getRailType(), title));
            }
        }

        for(final ChunkXZ chunkOffset : overlapChecklist) {
            final ChunkXZ offsettedChunkXZ = chunkXZ.getSum(chunkOffset);
            if(map.containsKey(offsettedChunkXZ)) {
                for(final OrdinalRailWay _ordinalRailWay : map.get(offsettedChunkXZ)) {
                    if (_ordinalRailWay.isAnotherWayOfTheSameType(ordinalRailWay)) {
                        throw new InvalidMapException(
                                String.format("Chunk %s is too close to another way %s of the same type %s in %s",
                                        chunkXZ, offsettedChunkXZ, railType, title));
                    }
                }
            }
        }

        ordinalRailWaySet.add(ordinalRailWay);
    }

    private final static Map<Integer,BlockFace> facing = ImmutableMap.<Integer,BlockFace>builder()
            .put(BlockFace.EAST.getModX() * 4 + BlockFace.EAST.getModZ(), BlockFace.EAST)
            .put(BlockFace.NORTH.getModX() * 4 + BlockFace.NORTH.getModZ(), BlockFace.NORTH)
            .put(BlockFace.WEST.getModX() * 4 + BlockFace.WEST.getModZ(), BlockFace.WEST)
            .put(BlockFace.SOUTH.getModX() * 4 + BlockFace.SOUTH.getModZ(), BlockFace.SOUTH)
            .build();

    BlockFace getBlockFace(final int modX, final int modZ) {
        final BlockFace blockFace = facing.get(modX * 4 + modZ);
        if(blockFace == null) {
            throw new InternalConfigurationError(String.format("Wrong block face mod %d-%d in %s", modX, modZ, title));
        }

        return blockFace;
    }

    static int getMod(int value) {
        return Integer.signum(value);
    }

    public ChunkMap addWay(final int x1, final int z1, final int x2, final int z2, final RailType railType) throws InvalidMapException {
        wayId++;

        if((x1 == x2) && (z1 == z2)) {
            throw new InvalidMapException(
                    String.format("Zero-length way from %s to %s in %s",
                            new ChunkXZ(x1, z1), new ChunkXZ(x2, z2), title));
        }

        final int modX = getMod(x2 - x1);
        final int modZ = getMod(z2 - z1);

        int x = x1;
        while(x != x2) {
            add(x, z1, railType, getBlockFace(modX, 0));
            x += modX;
        }

        if (x1 != x2) {
            add(x2, z1, railType, getBlockFace(modX, 0));
        } else {
            add(x2, z1, railType, getBlockFace(0, modZ));
        }

        int z = z1;
        while(z != z2) {
            z += modZ;
            add(x2, z, railType, getBlockFace(0, modZ));
        }

        return this;
    }

    public boolean containsRailWays(final int x, final int z) {
        return map.containsKey(new ChunkXZ(x, z));
    }

    public interface RailWayCallback {
        void call(final RailType railType, final BlockFace blockFace);
    }

    public void forEach(final int x, final int z, final RailWayCallback railWayCallback) {
        final OrdinalRailWaySet ordinalRailWaySet = map.get(new ChunkXZ(x, z));
        if(ordinalRailWaySet != null) {
            for (final OrdinalRailWay ordinalRailWay : ordinalRailWaySet) {
                railWayCallback.call(ordinalRailWay.getRailType(), ordinalRailWay.getBlockFace());
            }
        }
    }

    public interface XZRailWayCallback {
        void call(final int x, final int z, final RailType railType, final BlockFace blockFace);
    }

    public void forEach(final XZRailWayCallback xzRailWayCallback) {
        map.forEach((final ChunkXZ chunkXZ, final OrdinalRailWaySet ordinalRailWaySet) -> {
            ordinalRailWaySet.forEach((final OrdinalRailWay ordinalRailWay) -> {
                xzRailWayCallback.call(
                        chunkXZ.getX(),
                        chunkXZ.getZ(),
                        ordinalRailWay.getRailType(),
                        ordinalRailWay.getBlockFace());
            });
        });
    }

    @Override
    public String toString() {
        return String.format("%s%s", title, map);
    }
}
