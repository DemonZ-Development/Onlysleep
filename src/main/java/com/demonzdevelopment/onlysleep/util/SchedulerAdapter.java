package com.demonzdevelopment.onlysleep.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * A scheduler adapter that works across Bukkit, Spigot, Paper, and Folia servers.
 * Uses Folia's region scheduler on Folia servers and falls back to Bukkit scheduler on others.
 */
public final class SchedulerAdapter {

    private static boolean foliaChecked = false;
    private static boolean isFolia = false;

    private SchedulerAdapter() {}

    private static boolean isFolia() {
        if (!foliaChecked) {
            isFolia = PlatformAdapter.isFolia();
            foliaChecked = true;
        }
        return isFolia;
    }

    /**
     * Runs a task after a delay for a specific world.
     */
    public static ScheduledTask runTaskLater(JavaPlugin plugin, World world, Runnable task, long delay) {
        if (isFolia()) {
            return runFoliaTask(plugin, world, task, delay, 0);
        }
        if (delay <= 0) {
            BukkitTask bt = Bukkit.getScheduler().runTask(plugin, task);
            return new BukkitScheduledTask(bt);
        }
        BukkitTask bt = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return new BukkitScheduledTask(bt);
    }

    /**
     * Runs a repeating task for a specific world.
     */
    public static ScheduledTask runTaskTimer(JavaPlugin plugin, World world, Runnable task, long delay, long period) {
        if (isFolia()) {
            return runFoliaTask(plugin, world, task, delay, period);
        }
        if (delay <= 0 && period <= 0) {
            BukkitTask bt = Bukkit.getScheduler().runTask(plugin, task);
            return new BukkitScheduledTask(bt);
        }
        BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return new BukkitScheduledTask(bt);
    }

    /**
     * Runs a global task (not tied to a specific world).
     * On Folia, uses the global region scheduler.
     */
    public static ScheduledTask runGlobalTask(JavaPlugin plugin, Runnable task) {
        if (isFolia()) {
            return runFoliaGlobalTask(plugin, task, 0, 0);
        }
        BukkitTask bt = Bukkit.getScheduler().runTask(plugin, task);
        return new BukkitScheduledTask(bt);
    }

    /**
     * Runs a global repeating task.
     */
    public static ScheduledTask runGlobalTaskTimer(JavaPlugin plugin, Runnable task, long delay, long period) {
        if (isFolia()) {
            return runFoliaGlobalTask(plugin, task, delay, period);
        }
        BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return new BukkitScheduledTask(bt);
    }

    // --- Folia-specific implementations using reflection ---

    private static ScheduledTask runFoliaTask(JavaPlugin plugin, World world, Runnable task, long delay, long period) {
        try {
            Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
            java.util.concurrent.atomic.AtomicReference<Object> taskRef = new java.util.concurrent.atomic.AtomicReference<>();

            Consumer<Object> consumer = scheduledTask -> {
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                task.run();
            };

            if (period > 0) {
                Method runAtFixedRate = regionScheduler.getClass().getMethod(
                    "runAtFixedRate", org.bukkit.plugin.Plugin.class, World.class, Consumer.class, long.class, long.class
                );
                Object scheduledTask = runAtFixedRate.invoke(regionScheduler, plugin, world, consumer, delay, period);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            } else if (delay > 0) {
                Method runDelayed = regionScheduler.getClass().getMethod(
                    "runDelayed", org.bukkit.plugin.Plugin.class, World.class, Consumer.class, long.class
                );
                Object scheduledTask = runDelayed.invoke(regionScheduler, plugin, world, consumer, delay);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            } else {
                Method run = regionScheduler.getClass().getMethod(
                    "run", org.bukkit.plugin.Plugin.class, World.class, Consumer.class
                );
                Object scheduledTask = run.invoke(regionScheduler, plugin, world, consumer);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            }
        } catch (Exception e) {
            // Fallback to Bukkit scheduler if reflection fails
            if (period > 0) {
                BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
                return new BukkitScheduledTask(bt);
            } else if (delay > 0) {
                BukkitTask bt = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
                return new BukkitScheduledTask(bt);
            } else {
                BukkitTask bt = Bukkit.getScheduler().runTask(plugin, task);
                return new BukkitScheduledTask(bt);
            }
        }
    }

    private static ScheduledTask runFoliaGlobalTask(JavaPlugin plugin, Runnable task, long delay, long period) {
        try {
            Object globalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            java.util.concurrent.atomic.AtomicReference<Object> taskRef = new java.util.concurrent.atomic.AtomicReference<>();

            Consumer<Object> consumer = scheduledTask -> {
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                task.run();
            };

            if (period > 0) {
                Method runAtFixedRate = globalRegionScheduler.getClass().getMethod(
                    "runAtFixedRate", org.bukkit.plugin.Plugin.class, Consumer.class, long.class, long.class
                );
                Object scheduledTask = runAtFixedRate.invoke(globalRegionScheduler, plugin, consumer, delay, period);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            } else if (delay > 0) {
                Method runDelayed = globalRegionScheduler.getClass().getMethod(
                    "runDelayed", org.bukkit.plugin.Plugin.class, Consumer.class, long.class
                );
                Object scheduledTask = runDelayed.invoke(globalRegionScheduler, plugin, consumer, delay);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            } else {
                Method run = globalRegionScheduler.getClass().getMethod(
                    "run", org.bukkit.plugin.Plugin.class, Consumer.class
                );
                Object scheduledTask = run.invoke(globalRegionScheduler, plugin, consumer);
                if (scheduledTask != null) {
                    taskRef.compareAndSet(null, scheduledTask);
                }
                return new FoliaScheduledTask(taskRef);
            }
        } catch (Exception e) {
            // Fallback
            if (period > 0) {
                BukkitTask bt = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
                return new BukkitScheduledTask(bt);
            } else if (delay > 0) {
                BukkitTask bt = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
                return new BukkitScheduledTask(bt);
            } else {
                BukkitTask bt = Bukkit.getScheduler().runTask(plugin, task);
                return new BukkitScheduledTask(bt);
            }
        }
    }

    // --- ScheduledTask wrapper interfaces ---

    public interface ScheduledTask {
        void cancel();
        boolean isCancelled();
    }

    private static class BukkitScheduledTask implements ScheduledTask {
        private final BukkitTask task;

        BukkitScheduledTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private static class FoliaScheduledTask implements ScheduledTask {
        private final java.util.concurrent.atomic.AtomicReference<Object> taskRef;
        private volatile boolean cancelled = false;

        FoliaScheduledTask(java.util.concurrent.atomic.AtomicReference<Object> taskRef) {
            this.taskRef = taskRef;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
            Object taskObject = taskRef.get();
            if (taskObject == null) return;
            try {
                Method cancel = taskObject.getClass().getMethod("cancel");
                cancel.invoke(taskObject);
            } catch (Exception ignored) {
            }
        }

        @Override
        public boolean isCancelled() {
            Object taskObject = taskRef.get();
            if (taskObject != null) {
                try {
                    Method isCancelled = taskObject.getClass().getMethod("isCancelled");
                    return (boolean) isCancelled.invoke(taskObject);
                } catch (Exception ignored) {
                }
            }
            return cancelled;
        }
    }
}
