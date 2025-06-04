package com.gmail.uprial.secretrooms.populator;

import com.gmail.uprial.secretrooms.SecretRooms;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkQueue extends BukkitRunnable {
    private static final int INTERVAL = 1;

    private static final Queue<Runnable> DEFERRED_TASKS = new LinkedBlockingQueue<>();
    private static final Queue<Runnable> ACTIVE_TASKS = new LinkedBlockingQueue<>();

    ChunkQueue(final SecretRooms plugin) {
        runTaskTimer(plugin, INTERVAL, INTERVAL);
    }

    public static void add(final Runnable task) {
        DEFERRED_TASKS.add(task);
    }

    @Override
    public void cancel() {
        super.cancel();

        while(!DEFERRED_TASKS.isEmpty() || !ACTIVE_TASKS.isEmpty()) {
            run();
        }
    }

    @Override
    public void run() {
        Runnable task;
        while((task = ACTIVE_TASKS.poll()) != null) {
            task.run();
        }

        while((task = DEFERRED_TASKS.poll()) != null) {
            ACTIVE_TASKS.add(task);
        }
    }
}