package com.gmail.uprial.railnet.populator.endmansion;

import com.gmail.uprial.railnet.common.BlockSeed;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.common.WorldName;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.ChunkXZ;
import com.gmail.uprial.railnet.populator.Tested_On_1_21_5;
import org.bukkit.Chunk;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class EndMansionPopulator implements ChunkPopulator, Tested_On_1_21_5 {
    private final static String WORLD = WorldName.END;

    private final static int RADIAL_STEP = 1_000 / 16;
    private final static int RADIUS = 64 / 16;

    private final CustomLogger customLogger;

    public EndMansionPopulator(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    private static class EndMansionMap extends HashMap<ChunkXZ, EndMansionChunk> {};

    private final Map<Integer,EndMansionMap> stepCache = new HashMap<>();

    @Override
    public void populate(final Chunk chunk) {
        if(chunk.getWorld().getName().equals(WORLD)) {
            final int step = (int)Math.round(Math.sqrt(sqr(chunk.getX()) + sqr(chunk.getZ())) / RADIAL_STEP);

            if(step > 0) {
                final int x = getX(step, chunk);

                final int z = getZ(step, x);

                final EndMansionMap map = stepCache.computeIfAbsent(step, (k) -> getMap(x, z));

                final EndMansionChunk c = map.get(new ChunkXZ(chunk.getX(), chunk.getZ()));

                if (c != null) {
                    c.populate(chunk);

                    if (customLogger.isDebugMode()) {
                        customLogger.debug(String.format("%s[%s] populated", getName(), format(chunk)));
                    }
                }
            }
        }
    }

    public String getName() {
        return "EndMansion";
    }

    int getX(final int step, final Chunk chunk) {
        return step
                * ((int) BlockSeed.valueOf(chunk.getWorld())
                .oneOf(RADIAL_STEP * 2 + 1) - RADIAL_STEP);
    }

    int getZ(final int step, final int x) {
        return  (int) Math.sqrt(sqr(step * RADIAL_STEP) - sqr(x));
    }

    private EndMansionMap getMap(final int x, final int z) {
        final EndMansionMap map = new EndMansionMap();

        map.put(new ChunkXZ(x, z), new EndMansionBase(BlockFace.NORTH));
        for(int i = 1; i < RADIUS; i++) {
            map.put(new ChunkXZ(x, z - i), new EndMansionRoad(BlockFace.NORTH));
            map.put(new ChunkXZ(x, z + i), new EndMansionRoad(BlockFace.SOUTH));
        }
        map.put(new ChunkXZ(x, z - RADIUS), new EndMansionCorner(BlockFace.NORTH));
        map.put(new ChunkXZ(x, z + RADIUS), new EndMansionCorner(BlockFace.SOUTH));
        for(int i = 1; i < RADIUS / 2; i++) {
            map.put(new ChunkXZ(x - i, z - RADIUS), new EndMansionRoad(BlockFace.WEST));
            map.put(new ChunkXZ(x + i, z + RADIUS), new EndMansionRoad(BlockFace.EAST));
        }
        map.put(new ChunkXZ(x - RADIUS / 2, z - RADIUS), new EndMansionStop(BlockFace.WEST));
        map.put(new ChunkXZ(x + RADIUS / 2, z + RADIUS), new EndMansionStop(BlockFace.EAST));

        return map;
    }

    private double sqr(final double x) {
        return Math.pow(x, 2.0D);
    }
}