package com.gmail.uprial.railnet.populator;

import com.gmail.uprial.railnet.RailNet;
import com.gmail.uprial.railnet.common.CustomLogger;
import com.gmail.uprial.railnet.map.ChunkMap;
import com.gmail.uprial.railnet.map.InvalidMapException;
import com.gmail.uprial.railnet.map.RailType;
import com.gmail.uprial.railnet.schema.SchemaDebug;
import com.google.common.collect.ImmutableList;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.util.StructureSearchResult;

import java.util.*;

import static com.gmail.uprial.railnet.common.Formatter.format;

public class Populator {
    private static final int LOCATE_RADIUS = 10_000;

    static class WayConfig {
        final private String name;
        final private String world;
        final private RailType railType;
        final private StructureType from;
        final private StructureType to;

        WayConfig(final String name, final String world, final RailType railType, final StructureType from, final StructureType to) {
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

    private final RailNet plugin;
    private final CustomLogger customLogger;

    private final Map<World, ChunkMap> map = new HashMap<>();

    public Populator(final RailNet plugin, final CustomLogger customLogger) {
        this.plugin = plugin;
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
            final List<WayConfig> connectionsConfig = ImmutableList.<WayConfig>builder()
                    .add(new WayConfig("base2mansion", "world", RailType.UNDERGROUND, null, StructureType.WOODLAND_MANSION))
                    .add(new WayConfig("base2monument", "world", RailType.SURFACE, null, StructureType.OCEAN_MONUMENT))
                    .build();

            final Set<String> worldNames = new HashSet<>();
            for(final WayConfig wayConfig : connectionsConfig) {
                final String title = String.format("'%s' way", wayConfig.getName());
                final World world = plugin.getServer().getWorld(wayConfig.getWorld());
                if(world == null) {
                    throw new InternalPopulatorError(
                            String.format("Can't find world '%s' for %s", wayConfig.getWorld(), title)
                    );
                }
                final Location from = locate(title, world, world.getSpawnLocation(), wayConfig.getFrom());
                final Location to = locate(title, world, from, wayConfig.getTo());

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
                        wayConfig.getRailType());

                worldNames.add(world.getName());
            }

            // TOFIX: comment
            // Test ways

            /*
            final World world = plugin.getServer().getWorld("world");
            final ChunkMap chunkMap = map.computeIfAbsent(world, k -> new ChunkMap());
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

            for(final String worldName : worldNames) {
                onWorldInit(plugin.getServer().getWorld(worldName));
            }
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
                throw new InternalPopulatorError(
                        String.format("Can't locate %s near %s in world '%s' for %s",
                                structureType.getKey(), format(from), world.getName(), title)
                );
            }

            return structureSearchResult.getLocation();
        }
    }

    /*
        Plugins are enabled on server start.

        But some chunks are already generated
        before the first initiation of any plugin
        unless the plugin is configured in bukkit.yml as a generator.

        The following potential functions are buggy:
            world.getChunkAt(x, y, generate = false)
            world.isChunkGenerated(x, y)
            etc.

        So I check the chunks loaded on server start.
     */
    private void onWorldInit(final World world) {
        final ChunkMap chunkMap = map.get(world);
        if(chunkMap != null) {
            for(final Chunk chunk : world.getLoadedChunks()) {
                onChunkLoad(chunk);
            }
        }
    }

    /*
        ChunkPopulateEvent and ChunkLoadEvent.isNewChunk() are buggy,
        so I mark the checked chunks via specific secret block.

        The best specific secret block is a block unavailable even in the creative code.
        According to https://minecraft.wiki/w/Creative, I picked Barrier.
     */
    final Material secretMaterial = Material.BARRIER;

    public void onChunkLoad(final Chunk chunk) {
        final ChunkMap chunkMap = map.get(chunk.getWorld());
        if(chunkMap != null) {
            if(chunkMap.containsRailWays(chunk.getX(), chunk.getZ())) {
                final Block block = chunk.getBlock(0, chunk.getWorld().getMinHeight(), 0);
                if (!block.getType().equals(secretMaterial)) {
                    block.setType(secretMaterial);

                    populateChunk(chunkMap, chunk);
                }
            }
        }
    }

    private void populateChunk(final ChunkMap chunkMap, final Chunk chunk) {
        chunkMap.forEach(chunk.getX(), chunk.getZ(), (final RailType railType, final BlockFace blockFace) -> {
            populate(chunkMap, chunk, railType, blockFace);
        });
    }

    public void forciblyPopulate() {
        customLogger.debug("Forcibly populate...");
        for(final World world : plugin.getServer().getWorlds()) {
            final ChunkMap chunkMap = map.get(world);
            if(chunkMap != null) {
                chunkMap.forEach((final int x, final int z, final RailType railType, final BlockFace blockFace) -> {
                    populate(chunkMap, world.getChunkAt(x, z), railType, blockFace);
                });
            }
        }
    }

    public void populateLoaded() {
        customLogger.debug("Populate loaded...");
        for(final World world : plugin.getServer().getWorlds()) {
            final ChunkMap chunkMap = map.get(world);
            if(chunkMap != null) {
                for (final Chunk chunk : world.getLoadedChunks()) {
                    populateChunk(chunkMap, chunk);
                }
            }
        }
    }

    private void populate(final ChunkMap chunkMap, final Chunk chunk, final RailType railType, final BlockFace blockFace) {
        new StructurePopulator(chunkMap, chunk, railType, blockFace).populate();

        if(customLogger.isDebugMode()) {
            customLogger.debug(String.format("Populated %d-%d with %s-%s", chunk.getX(), chunk.getZ(), railType, blockFace));
        }
    }
}