/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.task;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncTaskWorker {
    private final LinkedList<Task> tasks;
    private final HashSet<String> jobsDone = new HashSet<>();
    private String name = "AsyncTaskExecutor";
    private ExceptionRunnable exceptionRunnable;

    public AsyncTaskWorker(LinkedList<Task> tasks) {
        this.tasks = tasks;
    }

    public AsyncTaskWorker(LinkedList<Task> tasks, String name) {
        this(tasks);
        this.name = name;
    }

    public AsyncTaskWorker() {
        this.tasks = new LinkedList<>();
    }

    public AsyncTaskWorker(String name) {
        this();
        this.name = name;
    }

    public LinkedList<Task> getTasks() {
        return this.tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void work(CountUpAndDownLatch progress) {
        this.tasks.sort((a, b) -> {
            if (a == null || b == null) {
                return 0;
            }
            return -(a.getPriority().ordinal() - b.getPriority().ordinal());
        });
        ConcurrentLinkedQueue<Task> doing = new ConcurrentLinkedQueue<>(this.tasks);
        CountUpAndDownLatch latch = new CountUpAndDownLatch(doing.size());
        while (!doing.isEmpty()) {
            doing.forEach((task -> {
                AtomicBoolean canStart = new AtomicBoolean(false);
                while (!canStart.get()) {
                    canStart.set(true);
                    task.getDependsOns().forEach((dependency) -> {
                        if (!this.jobsDone.contains(dependency)) {
                            canStart.set(false);
                        }
                    });
                    if (!canStart.get()) {
                        return;
                    }
                    Minosoft.THREAD_POOL.execute(() -> {
                        try {
                            task.getTask().work(progress);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (task.getImportance() == TaskImportance.REQUIRED) {
                                Log.fatal(String.format("Task %s (%s) failed: %s", task.getTaskName(), task.getTaskDescription(), e.getMessage()));
                                if (this.exceptionRunnable != null) {
                                    this.exceptionRunnable.onFatal(e);
                                }
                                throw new RuntimeException(e);
                            }
                        }
                        this.jobsDone.add(task.getTaskName());
                        latch.dec();
                    });
                    doing.remove(task);
                }
            }));
            latch.waitForChange();
        }
        progress.dec(); // remove initial value of 1
    }

    public boolean isJobDone(String name) {
        return this.jobsDone.contains(name);
    }

    public void setFatalError(ExceptionRunnable exceptionRunnable) {
        this.exceptionRunnable = exceptionRunnable;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public interface ExceptionRunnable {
        void onFatal(Exception e);
    }
}
