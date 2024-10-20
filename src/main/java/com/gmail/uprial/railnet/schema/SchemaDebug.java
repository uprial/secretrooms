package com.gmail.uprial.railnet.schema;

import com.gmail.uprial.railnet.common.CustomLogger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.StructureSearchResult;

import java.util.HashMap;
import java.util.Map;

public class SchemaDebug {
    final CustomLogger customLogger;

    public SchemaDebug(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    public void info(final World world) {
        customLogger.debug(String.format("Generating schema debug info for '%s'...", world.getName()));

        final Location from = world.getSpawnLocation();

        {
            final Map<Biome,Integer> biomes = new HashMap<>();
            for (final BiomeEnum biomeEnum : BiomeEnum.values()) {
                final BiomeSearchResult biomeSearchResult
                        // WARNING: for some reason with radius more 2_000 the servers hangs
                        = world.locateNearestBiome(from, 1_000, biomeEnum.getBiome());
                if (null != biomeSearchResult) {
                    biomes.put(biomeEnum.getBiome(),
                            (int)Math.round(from.distance(biomeSearchResult.getLocation())));
                }
            }
            customLogger.debug(String.format("Biomes found in '%s': %s", world.getName(), biomes));
        }

        {
            final Map<NamespacedKey,Integer> structures = new HashMap<>();
            for (final StructureEnum structureEnum : StructureEnum.values()) {
                final StructureSearchResult structureSearchResult
                        = world.locateNearestStructure(from, structureEnum.getStructure(), 1000_000, false);
                if (null != structureSearchResult) {
                    structures.put(structureEnum.getStructure().getKey(),
                            (int)Math.round(from.distance(structureSearchResult.getLocation())));
                }
            }
            customLogger.debug(String.format("Structure found in '%s': %s", world.getName(), structures));
        }

        {
            final Map<NamespacedKey,Integer> structureTypes = new HashMap<>();
            for (final StructureTypeEnum structureTypeEnum : StructureTypeEnum.values()) {
                final StructureSearchResult structureSearchResult
                        = world.locateNearestStructure(from, structureTypeEnum.getStructureType(), 1000_000, false);
                if (null != structureSearchResult) {
                    structureTypes.put(structureTypeEnum.getStructureType().getKey(),
                            (int)Math.round(from.distance(structureSearchResult.getLocation())));
                }
            }
            customLogger.debug(String.format("Structure types found in '%s': %s", world.getName(), structureTypes));
        }
    }
}
