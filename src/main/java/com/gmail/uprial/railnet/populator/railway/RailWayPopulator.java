package com.gmail.uprial.railnet.populator.railway;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.populator.ChunkPopulator;
import com.gmail.uprial.railnet.populator.railway.map.ChunkMap;
import com.gmail.uprial.railnet.populator.railway.map.InvalidMapException;
import com.gmail.uprial.railnet.populator.railway.map.RailType;
import com.gmail.uprial.railnet.populator.railway.schema.SchemaDebug;
import com.google.common.collect.ImmutableList;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;

import java.util.*;

public class RailWayPopulator implements ChunkPopulator {
    private static final int LOCATE_RADIUS = 10_000;

    static class RailWayConfig {
        final private String name;
        final private String world;
        final private RailType railType;
        final private StructureType from;
        final private StructureType to;

        RailWayConfig(final String name, final String world, final RailType railType, final StructureType from, final StructureType to) {
            this.name = name;
            this.world = world;
            this.railType = railType;
            this.from = from;
            this.to = to;
        }

        final String getName() {
            return name;
        }

        final String getWorld() {
            return world;
        }

        final RailType getRailType() {
            return railType;
        }

        final StructureType getFrom() {
            return from;
        }

        final StructureType getTo() {
            return to;
        }
    }

    private final CustomLogger customLogger;

    private final Map<World, ChunkMap> map = new HashMap<>();

    public RailWayPopulator(final RailNet plugin, final CustomLogger customLogger) {
        this.customLogger = customLogger;

        // TOFIX: comment
        // Heavy debug

        if(customLogger.isDebugMode()) {
            final SchemaDebug schemaDebug = new SchemaDebug(customLogger);
            for (World world : plugin.getServer().getWorlds()) {
                schemaDebug.info(world);
            }
        }

        try {
            /*
                Unfortunately, many biomes, structures, and structure types can't be located.

                According to the heavy debug above, only the following can be located:
                - mansion
                - stronghold
                - monument
                - buried_treasure

                The stronghold and buried treasure are required for vanilla progress,
                and it'd be unfair to highlight them via a railway.

                So, only two railways make sense.
             */
            final List<RailWayConfig> connectionsConfig = ImmutableList.<RailWayConfig>builder()
                    .add(new RailWayConfig("base2mansion", "world", RailType.UNDERGROUND, null, StructureType.WOODLAND_MANSION))
                    .add(new RailWayConfig("base2monument", "world", RailType.SURFACE, null, StructureType.OCEAN_MONUMENT))
                    .build();

            for(final RailWayConfig railWayConfig : connectionsConfig) {
                final String title = String.format("'%s' way", railWayConfig.getName());
                final World world = plugin.getServer().getWorld(railWayConfig.getWorld());
                if(world == null) {
                    throw new RailWayPopulatorError(
                            String.format("Can't find world '%s' for %s", railWayConfig.getWorld(), title)
                    );
                }
                final Location from = locate(title, world, world.getSpawnLocation(), railWayConfig.getFrom());
                final Location to = locate(title, world, from, railWayConfig.getTo());

                customLogger.info(String.format("Discovered %s in world '%s' from %s to %s",
                        title,
                        world.getName(),
                        format(from),
                        format(to)
                ));

                final ChunkMap chunkMap = map.computeIfAbsent(world, k -> new ChunkMap(title));

                final int modX = Integer.signum(to.getChunk().getX() - from.getChunk().getX());
                final int modZ = Integer.signum(to.getChunk().getZ() - from.getChunk().getZ());

                chunkMap.addWay(
                        from.getChunk().getX() - modX,
                        from.getChunk().getZ() - modZ,
                        to.getChunk().getX() - 3 * modX,
                        to.getChunk().getZ() - 3 * modZ,
                        railWayConfig.getRailType());
            }

            // TOFIX: comment
            // Test ways

            /*
            final World world = plugin.getServer().getWorld("world");
            final ChunkMap chunkMap = map.computeIfAbsent(world, k -> new ChunkMap("test way"));
            chunkMap.addWay(
                    1, -2,
                    1+3, -2-2,
                    RailType.SURFACE);
            chunkMap.addWay(
                    -1, -2,
                    -1-3, -2-2,
                    RailType.SURFACE);
            chunkMap.addWay(
                    0, -5,
                    0, -6,
                    RailType.SURFACE);
            chunkMap.addWay(
                    2, -5,
                    2, -4,
                    RailType.SURFACE);
            chunkMap.addWay(
                    3, -7,
                    4, -7,
                    RailType.SURFACE);
            chunkMap.addWay(
                    3, -9,
                    2, -9,
                    RailType.SURFACE);
            */
        } catch (InvalidMapException e) {
            throw new RuntimeException(e);
        }
    }

    private Location locate(final String title, final World world, final Location from, final StructureType structureType) {
        if(structureType == null) {
            return from;
        } else {
            final StructureSearchResult structureSearchResult = world.locateNearestStructure(
                from,
                    structureType,
                    LOCATE_RADIUS,
                false);
            if(structureSearchResult == null) {
                throw new RailWayPopulatorError(
                        String.format("Can't locate %s near %s in world '%s' for %s",
                                structureType.getKey(), format(from), world.getName(), title)
                );
            }

            return structureSearchResult.getLocation();
        }
    }

    @Override
    public void populate(final Chunk chunk) {
        final ChunkMap chunkMap = map.get(chunk.getWorld());
        if(chunkMap != null) {
            chunkMap.forEach(chunk.getX(), chunk.getZ(), (final RailType railType, final BlockFace blockFace) -> {
                new RailWayChunk(chunkMap, chunk, railType, blockFace).populate();

                if(customLogger.isDebugMode()) {
                    customLogger.debug(String.format("RailWay %s:%d:%d populated with %s-%s",
                            chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), railType, blockFace));
                }
            });
        }
    }

    private static String format(Location location) {
        return String.format("%s:%.0f:%.0f:%.0f",
                location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ());
    }
}