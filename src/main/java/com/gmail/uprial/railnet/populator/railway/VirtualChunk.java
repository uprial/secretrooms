package com.gmail.uprial.railnet.populator.railway;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Rail;

import java.util.Map;

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

    VirtualChunk(final String title, final Chunk chunk, final BlockFace blockFace) {
        this.title = title;
        this.chunk = chunk;
        this.vBlockFace = blockFace;
    }

    void move(final int x, final int y, final int z) {
        vx += x;
        if(vx < 0) {
            throw new RailWayPopulatorError(
                    String.format("Negative virtual X of %d-%d chunk in %s: %d", chunk.getX(), chunk.getZ(), title, vx));
        }

        vy += y;

        vz += z;
        if(vz < 0) {
            throw new RailWayPopulatorError(
                    String.format("Negative virtual Z of %d-%d chunk in %s: %d", chunk.getX(), chunk.getZ(), title, vz));
        }
    }

    void rotate180() {
        vBlockFace = getBlockFacesSum(vBlockFace, BlockFace.WEST);
    }

    BlockFace getBlockFaceRotatedLeft() {
        return getBlockFacesSum(vBlockFace, BlockFace.NORTH);
    }

    void applyPhysicsOnce() {
        this.applyPhysicsOnce = true;
    }

    int getMaxX() {
        // With any X-positive move, getting access to this block will raise an exception
        if(vx > 0) {
            throw new RailWayPopulatorError(
                    String.format("Positive virtual X of %d-%d chunk in %s: %d", chunk.getX(), chunk.getZ(), title, vx));
        }
        return 15;
    }

    int getMaxZ() {
        // With any Z-positive move, getting access to this block will raise an exception
        if(vz > 0) {
            throw new RailWayPopulatorError(
                    String.format("Positive virtual Z of %d-%d chunk in %s: %d", chunk.getX(), chunk.getZ(), title, vz));
        }
        return 15;
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
                throw new RailWayPopulatorError(
                        String.format("Wrong block face %s in %s", vBlockFace, title));
        }
    }

    private Block getMovedAndRotated(final int x, final int y, final int z) {
        return getRotated(vx + x, vy + y, vz + z);
    }

    Block get(final int x, final int y, final int z) {
        return getMovedAndRotated(x, y, z);
    }

    Block set(final int x, final int y, final int z, final Material material) {
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

    Block set(final int x, final int y, final int z,
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
            throw new RailWayPopulatorError(
                    String.format("Block %s at %d-%d-%d can't be rotated in %s", material, x, y, z, title));
        }

        // Power rails
        if(blockData instanceof Powerable) {
            ((Powerable)blockData).setPowered(true);
        }

        block.setBlockData(blockData, applyPhysicsOnce);

        applyPhysicsOnce = false;

        return block;
    }

    int getMaxHeight() {
        return chunk.getWorld().getMaxHeight() - vy;
    }

    int getSeaLevel() {
        return chunk.getWorld().getSeaLevel() - vy;
    }

    int getChunkX() {
        return chunk.getX();
    }

    int getChunkZ() {
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
}
