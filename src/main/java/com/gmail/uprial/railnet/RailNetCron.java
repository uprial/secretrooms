package com.gmail.uprial.railnet;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class RailNetCron extends BukkitRunnable {
    private static final int INTERVAL = 1;
    private static final int BATCH_SIZE = 100;

    private static final Queue<Runnable> DEFERRED_TASKS = new LinkedBlockingQueue<>();
    private static final Queue<Runnable> ACTIVE_TASKS = new LinkedBlockingQueue<>();

    public RailNetCron(RailNet plugin) {
        runTaskTimer(plugin, INTERVAL, INTERVAL);
    }

    public static void defer(Runnable task) {
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

        int activated = 0;
        while(((task = DEFERRED_TASKS.poll()) != null) && (activated < BATCH_SIZE)) {
            ACTIVE_TASKS.add(task);
            activated++;
        }
    }
}