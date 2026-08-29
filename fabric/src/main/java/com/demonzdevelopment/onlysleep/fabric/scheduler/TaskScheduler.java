package com.demonzdevelopment.onlysleep.fabric.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class TaskScheduler {

    private final List<Task> tasks = new ArrayList<>();
    private long tick;

    public final class Task {
        private long nextRunTick;
        private final long period;
        private final Runnable action;
        private boolean cancelled;

        private Task(long nextRunTick, long period, Runnable action) {
            this.nextRunTick = nextRunTick;
            this.period = period;
            this.action = action;
        }

        public void cancel() {
            cancelled = true;
        }
    }

    public Task runLater(long delayTicks, Runnable action) {
        Task task = new Task(tick + Math.max(1, delayTicks), -1, action);
        tasks.add(task);
        return task;
    }

    public Task runTimer(long delayTicks, long period, Runnable action) {
        Task task = new Task(tick + Math.max(1, delayTicks), period, action);
        tasks.add(task);
        return task;
    }

    public void runSync(Runnable action) {
        runLater(1, action);
    }

    public void tickServer() {
        tick++;
        Iterator<Task> it = tasks.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (task.cancelled) {
                it.remove();
                continue;
            }
            if (tick < task.nextRunTick) continue;

            task.action.run();

            if (task.cancelled) {
                it.remove();
            } else if (task.period > 0) {
                task.nextRunTick = tick + task.period;
            } else {
                it.remove();
            }
        }
    }

    public void cancelAll() {
        tasks.forEach(Task::cancel);
        tasks.clear();
    }
}
