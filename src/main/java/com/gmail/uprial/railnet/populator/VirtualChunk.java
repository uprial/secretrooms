package com.gmail.uprial.railnet.populator;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;

import java.util.Map;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class VirtualChunk {
    private final String title;
    private final Chunk chunk;

    private BlockFace vBlockFace;
    private int vx = 0;
    private int vy = 0;
    private int vz = 0;

    /*
        According to https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Block.html#setType(org.bukkit.Material,boolean)
        The applyPhysics parameter is required to prevent triggering infinite chunk loads on border blocks.
     */
    private boolean applyPhysicsOnce = false;

    private static class VirtualChunkError extends RuntimeException {
        VirtualChunkError(String message) {
            super(message);
        }
    }

    public VirtualChunk(final String title, final Chunk chunk, final BlockFace blockFace) {
        this.title = title;
        this.chunk = chunk;
        this.vBlockFace = blockFace;
    }

    public void move(final int x, final int y, final int z) {
        vx += x;
        if(vx < 0) {
            throw new VirtualChunkError(
                    String.format("Negative virtual X of %s chunk in %s: %d", format(chunk), title, vx));
        }

        vy += y;

        vz += z;
        if(vz < 0) {
            throw new VirtualChunkError(
                    String.format("Negative virtual Z of %s chunk in %s: %d", format(chunk), title, vz));
        }
    }

    public void rotate180() {
        vBlockFace = getBlockFacesSum(vBlockFace, BlockFace.WEST);
    }

    public BlockFace getBlockFaceRotatedLeft() {
        return getBlockFacesSum(vBlockFace, BlockFace.NORTH);
    }

    public void applyPhysicsOnce() {
        this.applyPhysicsOnce = true;
    }

    public int getMaxX() {
        // With any X-positive move, getting access to this block will raise an exception
        if(vx > 0) {
            throw new VirtualChunkError(
                    String.format("Positive virtual X of %s chunk in %s: %d", format(chunk), title, vx));
        }
        return 15;
    }

    public int getMaxZ() {
        // With any Z-positive move, getting access to this block will raise an exception
        if(vz > 0) {
            throw new VirtualChunkError(
                    String.format("Positive virtual Z of %s chunk in %s: %d", format(chunk), title, vz));
        }
        return 15;
    }

    public World getWorld() {
        return chunk.getWorld();
    }

    private Block getRotated(final int x, final int y, final int z) {
        switch (vBlockFace) {
            case EAST:
                // 0:0 > 0:0
                // 1:0 > 1:0
                return chunk.getBlock(x, y, z);
            case WEST:
                // 0:0 > 15:15
                // 1:0 > 14:15
                return chunk.getBlock(15 - x, y, 15 - z);
            case NORTH:
                // 0:0 > 0:15
                // 1:0 > 0:14
                return chunk.getBlock(z, y, 15 - x);
            case SOUTH:
                // 0:0 > 15:0
                // 1:0 > 15:1
                return chunk.getBlock(15 - z, y, x);
            default:
                throw new VirtualChunkError(
                        String.format("Wrong block face %s in %s", vBlockFace, title));
        }
    }

    private Block getMovedAndRotated(final int x, final int y, final int z) {
        return getRotated(vx + x, vy + y, vz + z);
    }

    public Block get(final int x, final int y, final int z) {
        return getMovedAndRotated(x, y, z);
    }

    public Block set(final int x, final int y, final int z, final Material material) {
        final Block block = get(x, y, z);

        block.setType(material, applyPhysicsOnce);

        applyPhysicsOnce = false;

        return block;
    }

    private final static Map<BlockFace,Rail.Shape> blockFace2railShape = ImmutableMap.<BlockFace,Rail.Shape>builder()
            .put(BlockFace.EAST, Rail.Shape.EAST_WEST)
            .put(BlockFace.NORTH, Rail.Shape.NORTH_SOUTH)
            .put(BlockFace.WEST, Rail.Shape.EAST_WEST)
            .put(BlockFace.SOUTH, Rail.Shape.NORTH_SOUTH)
            .build();

    public Block set(final int x, final int y, final int z,
             final Material material, final BlockFace blockFace) {
        // Can't use set() due to applyPhysicsOnce limitations
        // final Block block = set(x, y, z, material);

        final Block block = get(x, y, z);
        block.setType(material, applyPhysicsOnce);

        final BlockData blockData = block.getBlockData();
        if(blockData instanceof Directional) {
            // Rotate blocks
            ((Directional)blockData).setFacing(getBlockFacesSum(vBlockFace, blockFace));
        } else if (blockData instanceof Rail) {
            // Rotate rails
            ((Rail)blockData).setShape(blockFace2railShape.get(getBlockFacesSum(vBlockFace, blockFace)));
        } else {
            throw new VirtualChunkError(
                    String.format("Block %s at %s:%d:%d:%d can't be rotated in %s",
                            material, chunk.getWorld().getName(), x, y, z, title));
        }

        // Power rails
        if(blockData instanceof Powerable) {
            ((Powerable)blockData).setPowered(true);
        }

        block.setBlockData(blockData, applyPhysicsOnce);

        applyPhysicsOnce = false;

        return block;
    }

    public int getMaxHeight() {
        return chunk.getWorld().getMaxHeight() - vy;
    }

    public int getMinHeight() {
        return chunk.getWorld().getMinHeight() - vy;
    }

    public int getSeaLevel() {
        return chunk.getWorld().getSeaLevel() - vy;
    }

    public int getChunkX() {
        return chunk.getX();
    }

    public int getChunkZ() {
        return chunk.getZ();
    }

    private final static Map<BlockFace,Integer> blockFace2clock = ImmutableMap.<BlockFace,Integer>builder()
            .put(BlockFace.EAST, 0)
            .put(BlockFace.NORTH, 1)
            .put(BlockFace.WEST, 2)
            .put(BlockFace.SOUTH, 3)
            .build();

    private static int getClock(final BlockFace blockFace) {
        return blockFace2clock.get(blockFace);
    }

    private final static Map<Integer,BlockFace> clock2blockFace = ImmutableMap.<Integer,BlockFace>builder()
            .put(0, BlockFace.EAST)
            .put(1, BlockFace.NORTH)
            .put(2, BlockFace.WEST)
            .put(3, BlockFace.SOUTH)
            .build();

    private static BlockFace getBlockFace(int clock) {
        return clock2blockFace.get(clock % 4);
    }

    private static BlockFace getBlockFacesSum(final BlockFace blockFace1, final BlockFace blockFace2) {
        return getBlockFace(getClock(blockFace1) + getClock(blockFace2));
    }

    public void box(final Material material,
                    final int x1, final int y1, final int z1,
                    final int x2, final int y2, final int z2) {

        final int rx1 = Math.min(x1, x2);
        final int rx2 = Math.max(x1, x2);
        final int ry1 = Math.min(y1, y2);
        final int ry2 = Math.max(y1, y2);
        final int rz1 = Math.min(z1, z2);
        final int rz2 = Math.max(z1, z2);

        for(int x = rx1; x <= rx2; x++) {
            for(int y = ry1; y <= ry2; y++) {
                for(int z = rz1; z <= rz2; z++) {
                    set(x, y, z, material);
                }
            }
        }
    }
}
