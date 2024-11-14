package com.gmail.uprial.railnet.populator.railway;

import com.gmail.uprial.railnet.populator.VirtualChunk;
import com.gmail.uprial.railnet.populator.railway.map.ChunkMap;
import com.gmail.uprial.railnet.populator.railway.map.RailType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class RailWayChunk {
    private final ChunkMap chunkMap;
    private final RailType railType;
    private final BlockFace blockFace;

    private final Set<BlockFace> neighbors;

    private final VirtualChunk vc;

    RailWayChunk(final ChunkMap chunkMap, final Chunk chunk, final RailType railType, final BlockFace blockFace) {
        this.chunkMap = chunkMap;
        this.railType = railType;
        this.blockFace = blockFace;

        this.vc = new VirtualChunk(chunkMap.getTitle(), chunk, blockFace);

        this.neighbors = getNeighbors();
    }

    /*
     The tunnel height is 6 blocks, so the offsets are multiple of 6.
     Two additional blocks are to hide surface tunnels underwater.
     */
    private final static Map<RailType,Integer> yOffsets = ImmutableMap.<RailType,Integer>builder()
            .put(RailType.UNDERGROUND, 14)
            .put(RailType.SURFACE, 8)
            .build();

    public void populate() {
        final Integer yOffset = yOffsets.get(railType);
        if(yOffset == null) {
            throw new RailWayPopulatorError(
                    String.format("Unknown rail type %s in %s", railType, chunkMap.getTitle()));
        }

        // Build under sea level.
        vc.move(0, vc.getSeaLevel() - yOffset, 0);
        if (isBorderChunk()) {
            if(!neighbors.contains(blockFace)) {
                vc.rotate180();
                // Max ladder and tunnel width is 6.
                vc.move(0, 0, vc.getMaxZ() - 6);
            }
            final int entranceX = ladder();
            initialTunnel(entranceX - 1);
        } else if (isCornerChunk()) {
            if(neighbors.contains(vc.getBlockFaceRotatedLeft())) {
                leftTunnel();
            } else {
                rightTunnel();
            }
        } else {
            tunnel(0);
        }
    }

    private Set<BlockFace> getNeighbors() {
        final Set<BlockFace> neighbors = new HashSet<>();
        final Set<BlockFace> potentialNeighbors = new HashSet<>();

        iterateNeighbors(vc.getChunkX(), vc.getChunkZ(), (final int ix, final int iz, final BlockFace blockFace) -> {
            potentialNeighbors.add(blockFace);
            chunkMap.forEach(ix, iz, (final RailType iRailType, final BlockFace iBlockFace) -> {
                if (iRailType == railType) {
                    neighbors.add(blockFace);
                }
            });
        });

        if(!potentialNeighbors.contains(blockFace)) {
            throw new RailWayPopulatorError(
                    String.format("Wrong block face %s in %s", blockFace, chunkMap.getTitle()));
        }

        return neighbors;
    }

    private boolean isBorderChunk() {
        return (neighbors.size() == 1);
    }

    private boolean isCornerChunk() {
        return (!isBorderChunk() && (!neighbors.contains(blockFace)));
    }

    private int getEntranceHeight(final int x, final int z) {
        final AtomicInteger height = new AtomicInteger(vc.getMaxHeight());

        while(height.intValue() > 0) {
            final AtomicInteger passableBlocks = new AtomicInteger(0);
            iterateNeighbors(x, z, (final int ix, final int iz) -> {
                if (vc.get(ix, height.intValue(), iz).isPassable()) {
                    passableBlocks.incrementAndGet();
                }
            });

            // The minimum number of passable blocks is 0, and the maximum is 4.
            if(passableBlocks.intValue() < 1) {
                break;
            }
            height.decrementAndGet();
        }

        return height.intValue();
    }

    private int ladder() {
        final int minEntranceY = 3;

        final int entranceX;
        final int entranceY;
        final int entranceZ = 1;
        {
        /*
            Start from x = 2 because one block is used for walls,
            and one more block is needed for proper work of drawObservationBox
            what checks the neighboring blocks.
         */
            int nonFinalEntranceX = 2;
            int nonFinalEntranceY = Math.max(minEntranceY, getEntranceHeight(nonFinalEntranceX, 1));
            {
            /*
                End at x = 11 because initialTunnel() requires at least 4 blocks
                in the positive X direction.
             */
                for (int x = 3; x <= 11; x++) {
                    int xEntranceY = Math.max(minEntranceY, getEntranceHeight(x, 1));
                    if (xEntranceY < nonFinalEntranceY) {
                        nonFinalEntranceY = xEntranceY;
                        nonFinalEntranceX = x;
                    }
                }
            }
            entranceX = nonFinalEntranceX;
            entranceY = nonFinalEntranceY;
        }

        {
            for(int y = 0; y <= entranceY; y++) {
                final int iy = y;
                iterateNeighbors(entranceX, entranceZ, (final int ix, final int iz) -> {
                    // I should've checked Z, but Z-1 isn't available in this chunk.
                    borderBlock(
                            Integer.signum(ix - entranceX) +  Integer.signum(iz - entranceZ), 0, 0,
                            ix, iy, iz);
                });

                set(entranceX, iy, entranceZ, Material.LADDER, BlockFace.SOUTH);

                // A torch in the wall
                if ((iy % 10 == 0) && (iy < entranceY)) {
                    set(entranceX + 1, iy, entranceZ, Material.WALL_TORCH, BlockFace.SOUTH);

                    borderBlock(
                            +1, 0, 0,
                            entranceX + 2, iy, entranceZ);

                    // I should've checked 0 0 -1, but Z-1 isn't available in this chunk.
                    borderBlock(
                            +1, 0, 0,
                            entranceX + 1, iy, entranceZ - 1);
                    borderBlock(
                                0, 0, +1,
                            entranceX + 1, iy, entranceZ + 1);
                    borderBlock(
                            +1, 0, 0,
                            entranceX + 1, iy - 1, entranceZ);
                }
            }

            // A block under ladder, if not two ladders go through it
            final Block block = vc.get(entranceX, - 1, entranceZ);
            if(!block.getType().equals(Material.LADDER)) {
                borderBlock(
                        0, -1, 0,
                        entranceX, - 1, entranceZ);
            }
        }

        {
            // A hole upwards, if any blocks are above the entrance
            int y = entranceY + 1;
            while (!vc.get(entranceX, y, entranceZ).isPassable()) {
                set(entranceX, y, entranceZ, Material.AIR);
                y++;
            }
        }

        {
            // A ladder upwards, if any blocks are behind the entrance
            int y = entranceY + 1;
            while (!vc.get(entranceX, y, 0).isPassable()) {
                set(entranceX, y, 1, Material.LADDER, BlockFace.SOUTH);
                y++;
            }
        }

        return entranceX;
    }

    private void tunnel(int startX) {
        // Tunnel
        drawBox(Material.AIR,
                startX, 0, 2,
                vc.getMaxX(), 2, 4);

        // Roof and floor
        for(int y = -1; y <= 3; y += 4) {
            borderBox(
                    0, Integer.signum(y), 0,
                    startX, y, 2,
                    vc.getMaxX(), y, 4);
        }

        // Side walls
        borderBox(
                0, 0, -1,
                startX, 0, 1,
                vc.getMaxX(), 2, 1);
        borderBox(
                0, 0, +1,
                startX, 0, 5,
                vc.getMaxX(), 2, 5);

        // Torches on the side walls
        set(startX + 4, 1, 2, Material.WALL_TORCH, BlockFace.SOUTH);
        set(startX + 4, 1, 4, Material.WALL_TORCH, BlockFace.NORTH);
        set(vc.getMaxX() - 4, 1, 2, Material.WALL_TORCH, BlockFace.SOUTH);
        set(vc.getMaxX() - 4, 1, 4, Material.WALL_TORCH, BlockFace.NORTH);

        // Typical rails
        drawBox(Material.RAIL, BlockFace.EAST,
                startX, 0, 2,
                vc.getMaxX(), 0, 2);
        drawBox(Material.RAIL, BlockFace.EAST,
                startX, 0, 4,
                vc.getMaxX(), 0, 4);

        /*
            Typical powered rails, once per 32 blocks, which is once per 2 chunks.

            CAUTION: REDSTONE_TORCH must be placed before POWERED_RAIL.
         */
        if(
                (blockFace == BlockFace.EAST || blockFace == BlockFace.WEST)
                        && (vc.getChunkX() % 2 == 0)
            ||
                (blockFace == BlockFace.NORTH || blockFace == BlockFace.SOUTH)
                        && (vc.getChunkZ() % 2 == 0)
        ) {
            set(startX + 1, 0, 3, Material.REDSTONE_TORCH);

            set(startX + 1, 0, 2, Material.POWERED_RAIL, BlockFace.EAST);
            set(startX + 1, 0, 4, Material.POWERED_RAIL, BlockFace.EAST);
        }
    }

    private void initialTunnel(int startX) {
        tunnel(startX);

        // Back wall
        borderBox(
                -1, 0, 0,
                startX, 0, 2,
                startX, 2, 4);

        /*
            Initial powered rails
            CAUTION: REDSTONE_TORCH must be placed before POWERED_RAIL.
         */
        set(startX + 1, 0, 3, Material.REDSTONE_TORCH);

        set(startX + 1, 0, 2, Material.RAIL, BlockFace.EAST);
        for(int ix = startX + 1; ix <= startX + 5; ix++) {
            set(ix, 0, 4, Material.POWERED_RAIL, BlockFace.EAST);
        }

        // A chest with minecarts
        final Block block = set(startX + 2, -1, 3, Material.CHEST, BlockFace.WEST);
        final Chest chest = (Chest)block.getState();
        final Inventory inventory = chest.getBlockInventory();
        for(int i = 0; i < 10; i++) {
            inventory.addItem(new ItemStack(Material.MINECART));
        }
    }

    private void leftTunnel() {
        // Tunnel
        drawBox(Material.AIR,
                0, 0, 2,
                4, 2, 4);
        drawBox(Material.AIR,
                2, 0, 1,
                4, 2, 0);
        drawBox(Material.AIR,
                1, 0, 1,
                1, 2, 1);

        // Roof and floor
        for(int y = -1; y <= 3; y += 4) {
            borderBox(
                    0, Integer.signum(y), 0,
                    0, y, 1,
                    4, y, 4);
            borderBox(
                    0, Integer.signum(y), 0,
                    1, y, 0,
                    4, y, 0);
        }

        // Side walls
        borderBox(
                0, 0, -1,
                0, 0, 1,
                0, 2, 1);
        borderBox(
                -1, 0, 0,
                1, 0, 0,
                1, 2, 0);

        borderBox(
                0, 0, +1,
                0, 0, 5,
                4, 2, 5);
        borderBox(
                +1, 0, 0,
                5, 0, 4,
                5, 2, 0);

        // Torches on the side walls
        set(3, 1, 4, Material.WALL_TORCH, BlockFace.NORTH);
        set(4, 1, 3, Material.WALL_TORCH, BlockFace.WEST);

        // Typical rails
        drawBox(Material.RAIL, BlockFace.EAST,
                0, 0, 2,
                1, 0, 2);
        drawBox(Material.RAIL, BlockFace.NORTH,
                2, 0, 1,
                2, 0, 0);

        // CAUTION: corner rail must be placed after straight two
        vc.applyPhysicsOnce();
        set(2, 0, 2, Material.RAIL);

        drawBox(Material.RAIL, BlockFace.EAST,
                0, 0, 4,
                3, 0, 4);
        drawBox(Material.RAIL, BlockFace.NORTH,
                4, 0, 3,
                4, 0, 0);

        // CAUTION: corner rail must be placed after straight two
        vc.applyPhysicsOnce();
        set(4, 0, 4, Material.RAIL);

        /*
            Powered rails.
            CAUTION: REDSTONE_TORCH must be placed before POWERED_RAIL.
         */
        set(1, 0, 3, Material.REDSTONE_TORCH);
        drawBox(Material.POWERED_RAIL, BlockFace.EAST,
                0, 0, 2,
                1, 0, 2);

        set(3, 0, 1, Material.REDSTONE_TORCH);
        drawBox(Material.POWERED_RAIL, BlockFace.NORTH,
                4, 0, 3,
                4, 0, 1);
    }

    private void rightTunnel() {
        // Tunnel
        drawBox(Material.AIR,
                0, 0, 2,
                vc.getMaxX() - 2, 2, 4);
        drawBox(Material.AIR,
                vc.getMaxX() - 4, 0, 2,
                vc.getMaxX() - 2, 2, vc.getMaxZ());
        drawBox(Material.AIR,
                vc.getMaxX() - 5, 0, 5,
                vc.getMaxX() - 5, 2, 5);

        // Roof and floor
        for(int y = -1; y <= 3; y += 4) {
            borderBox(
                    0, Integer.signum(y), 0,
                    0, y, 1,
                    vc.getMaxX() - 2, y, 4);
            borderBox(
                    0, Integer.signum(y), 0,
                    vc.getMaxX() - 2, y, 2,
                    vc.getMaxX() - 4, y, vc.getMaxZ());
        }

        // Side walls
        borderBox(
                0, 0, -1,
                0, 0, 1,
                vc.getMaxX() - 2, 2, 1);
        borderBox(
                +1, 0, 0,
                vc.getMaxX() - 1, 0, 1,
                vc.getMaxX() - 1, 2, vc.getMaxZ());

        borderBox(
                0, 0, +1,
                0, 0, 5,
                vc.getMaxX() - 6, 2, 5);
        borderBox(
                -1, 0, 0,
                vc.getMaxX() - 5, 0, 6,
                vc.getMaxX() - 5, 2, vc.getMaxZ());

        // Torches on the side walls
        set(4, 1, 2, Material.WALL_TORCH, BlockFace.SOUTH);
        set(4, 1, 4, Material.WALL_TORCH, BlockFace.NORTH);
        set(vc.getMaxX() - 6, 1, 2, Material.WALL_TORCH, BlockFace.SOUTH);
        set(vc.getMaxX() - 6, 1, 4, Material.WALL_TORCH, BlockFace.NORTH);
        set(vc.getMaxX() - 2, 1, 6, Material.WALL_TORCH, BlockFace.WEST);
        set(vc.getMaxX() - 4, 1, 6, Material.WALL_TORCH, BlockFace.EAST);
        set(vc.getMaxX() - 2, 1, vc.getMaxZ() - 4, Material.WALL_TORCH, BlockFace.WEST);
        set(vc.getMaxX() - 4, 1, vc.getMaxZ() - 4, Material.WALL_TORCH, BlockFace.EAST);

        // Typical rails
        drawBox(Material.RAIL, BlockFace.EAST,
                0, 0, 2,
                vc.getMaxX() - 3, 0, 2);
        drawBox(Material.RAIL, BlockFace.NORTH,
                vc.getMaxX() - 2, 0, 3,
                vc.getMaxX() - 2, 0, vc.getMaxZ());

        // CAUTION: corner rail must be placed after straight two
        vc.applyPhysicsOnce();
        set(vc.getMaxX() - 2, 0, 2, Material.RAIL);

        drawBox(Material.RAIL, BlockFace.EAST,
                0, 0, 4,
                vc.getMaxX() - 5, 0, 4);
        drawBox(Material.RAIL, BlockFace.NORTH,
                vc.getMaxX() - 4, 0, 5,
                vc.getMaxX() - 4, 0, vc.getMaxZ());

        // CAUTION: corner rail must be placed after straight two
        vc.applyPhysicsOnce();
        set(vc.getMaxX() - 4, 0, 4, Material.RAIL);

        /*
            Powered rails.
            CAUTION: REDSTONE_TORCH must be placed before POWERED_RAIL.
         */
        set(vc.getMaxX() - 4, 0, 3, Material.REDSTONE_TORCH);
        drawBox(Material.POWERED_RAIL, BlockFace.EAST,
                vc.getMaxX() - 3, 0, 2,
                vc.getMaxX() - 4, 0, 2);

        set(vc.getMaxX() - 3, 0, 6, Material.REDSTONE_TORCH);
        drawBox(Material.POWERED_RAIL, BlockFace.NORTH,
                vc.getMaxX() - 4, 0, 5,
                vc.getMaxX() - 4, 0, 6);
    }

    private interface XYZCallback {
        void call(final int x, final int y, final int z);
    }

    private void iterateBox(final int x1, final int y1, final int z1,
                            final int x2, final int y2, final int z2,
                            final XYZCallback callback) {

        final int rx1 = Math.min(x1, x2);
        final int rx2 = Math.max(x1, x2);
        final int ry1 = Math.min(y1, y2);
        final int ry2 = Math.max(y1, y2);
        final int rz1 = Math.min(z1, z2);
        final int rz2 = Math.max(z1, z2);

        for(int x = rx1; x <= rx2; x++) {
            for(int y = ry1; y <= ry2; y++) {
                for(int z = rz1; z <= rz2; z++) {
                    callback.call(x, y, z);
                }
            }
        }
    }

    private void drawBox(final Material material,
                         final int x1, final int y1, final int z1,
                         final int x2, final int y2, final int z2) {
        iterateBox(x1, y1, z1, x2, y2, z2, (final int x, final int y, final int z) -> {
            set(x, y, z, material);
        });
    }

    private void drawBox(final Material material, final BlockFace blockFace,
                         final int x1, final int y1, final int z1,
                         final int x2, final int y2, final int z2) {
        iterateBox(x1, y1, z1, x2, y2, z2, (final int x, final int y, final int z) -> {
            set(x, y, z, material, blockFace);
        });
    }

    private void borderBox(final int dx, final int dy, final int dz,
                           final int x1, final int y1, final int z1,
                           final int x2, final int y2, final int z2) {

        // Count potentially transparent blocks in the box
        final AtomicInteger transparent = new AtomicInteger(0);
        iterateBox(x1, y1, z1, x2, y2, z2, (final int x, final int y, final int z) -> {
            if(!vc.get(x + dx, y + dy, z + dz).getType().isOccluding()) {
                transparent.incrementAndGet();
            }
        });

        final int all = (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);

        // If more than 10% of blocks in the box can be transparent
        if(transparent.intValue() > all / 10) {
            iterateBox(x1, y1, z1, x2, y2, z2, (final int x, final int y, final int z) -> {
                borderBlock(dx, dy, dz, x, y, z);
            });
        } else {
            iterateBox(x1, y1, z1, x2, y2, z2, (final int x, final int y, final int z) -> {
                set(x, y, z, Material.STONE_BRICKS);
            });
        }
    }

    private void borderBlock(final int dx, final int dy, final int dz,
                             final int x, final int y, final int z) {
        final Material material;
        if(!vc.get(x + dx, y + dy, z + dz).getType().isOccluding()) {
            material = Material.GLASS;
        } else {
            material = Material.STONE_BRICKS;
        }
        set(x, y, z, material);
    }

    private void set(final int x, final int y, final int z,
                      final Material material) {
        checkOverrides(x, y, z, material, () -> vc.set(x, y, z, material));
    }

    private Block set(final int x, final int y, final int z,
                      final Material material, final BlockFace blockFace) {
        return checkOverrides(x, y, z, material, () -> vc.set(x, y, z, material, blockFace));
    }

    private final static Set<Material> importantMaterials = ImmutableSet.<Material>builder()
            .add(Material.LADDER)
            .add(Material.WALL_TORCH)
            .build();

    private final Map<Vector,Material> alreadySet = new HashMap<>();

    private interface BlockCallback {
        Block call();
    }

    static boolean isBorderBlock(final Material material) {
        return material == Material.GLASS || material == Material.STONE_BRICKS;
    }

    private Block checkOverrides(final int x, final int y, final int z,
                                 final Material material, final BlockCallback callback) {
        final Vector vector = new Vector(x, y, z);

        // Don't replace ladders and torches set in this population with stone bricks
        if(isBorderBlock(material)) {
            final Material alreadySetMaterial = alreadySet.get(vector);
            if((alreadySetMaterial != null)
                && importantMaterials.contains(alreadySetMaterial)) {
                return null;
            }
        }

        final Block block = callback.call();

        alreadySet.put(vector, material);

        return block;
    }

    private interface XZBlockFaceCallback {
        void call(final int x, final int z, final BlockFace blockFace);
    }

    private static void iterateNeighbors(final int x, final int z, final XZBlockFaceCallback callback) {
        callback.call(x - 1, z, BlockFace.WEST);
        callback.call(x + 1, z, BlockFace.EAST);
        callback.call(x, z - 1, BlockFace.NORTH);
        callback.call(x, z + 1, BlockFace.SOUTH);
    }

    private interface XZCallback {
        void call(final int x, final int z);
    }

    private static void iterateNeighbors(final int x, final int z, final XZCallback callback) {
        iterateNeighbors(x, z, (final int ix, final int iz, final BlockFace blockFace) -> callback.call(ix, iz));
    }
}
