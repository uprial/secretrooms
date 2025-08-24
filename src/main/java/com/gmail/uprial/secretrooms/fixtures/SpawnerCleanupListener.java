package com.gmail.uprial.secretrooms.fixtures;

import com.gmail.uprial.secretrooms.common.CustomLogger;
import com.gmail.uprial.secretrooms.populator.ChunkXZ;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Chunk;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.HashSet;
import java.util.Set;

import static com.gmail.uprial.secretrooms.common.Formatter.format;

// Used to fix mistakes when too good spawners were allowed.
public class SpawnerCleanupListener implements Listener {
    private final CustomLogger customLogger;

    public SpawnerCleanupListener(final CustomLogger customLogger) {
        this.customLogger = customLogger;
    }

    // According to https://minecraft.wiki/w/Monster_Spawner
    private static final Set<EntityType> ALLOWED_TYPES = ImmutableSet.<EntityType>builder()
            .add(EntityType.PIG)
            .add(EntityType.CAVE_SPIDER)
            .add(EntityType.SILVERFISH)
            .add(EntityType.ZOMBIE)
            .add(EntityType.SKELETON)
            .add(EntityType.BLAZE)
            .add(EntityType.SPIDER)
            .add(EntityType.MAGMA_CUBE)
            .add(EntityType.CREEPER)
            .build();

    private final Set<ChunkXZ> chunks = new HashSet<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(!event.isCancelled()
                && event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)
                && !ALLOWED_TYPES.contains(event.getEntityType())) {

            final Chunk chunk = event.getEntity().getLocation().getChunk();
            final ChunkXZ chunkXZ = new ChunkXZ(chunk.getX(), chunk.getZ());
            if(!chunks.contains(chunkXZ)) {
                customLogger.info(String.format("Suspicious spawn: %s", format(event.getEntity())));
                chunks.add(chunkXZ);
            }
        }
    }
}
