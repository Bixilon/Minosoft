/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.task.worker

import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import de.bixilon.minosoft.util.task.pool.ThreadPoolRunnable
import de.bixilon.minosoft.util.task.worker.tasks.Task

class TaskWorker(
    var errorHandler: (task: Task, exception: Throwable) -> Unit = { _, _ -> },
    var criticalErrorHandler: (task: Task, exception: Throwable) -> Unit = { _, _ -> },
) {
    private val todo: MutableMap<Any, Task> = synchronizedMapOf()
    var state: TaskWorkerStates = TaskWorkerStates.PREPARING
        private set

    operator fun plusAssign(task: Task) {
        check(state == TaskWorkerStates.PREPARING) { "Task worker is already working!" }
        todo[task.identifier] = task
    }


    fun work(progress: CountUpAndDownLatch) {
        val todo = this.todo.toSynchronizedMap().values.sortedWith { a, b -> a.priority - b.priority }.toMutableList()
        val done: MutableSet<Any> = synchronizedSetOf()
        var exit = false

        val workerProgress = CountUpAndDownLatch(1, progress)


        while (todo.isNotEmpty()) {
            var changed = false
            task@ for (task in todo.toSynchronizedList()) {

                for (dependency in task.dependencies) {
                    if (!done.contains(dependency)) {
                        continue@task
                    }
                }

                val taskProgress = CountUpAndDownLatch(2, workerProgress)
                todo -= task
                DefaultThreadPool += ThreadPoolRunnable(priority = task.priority) {
                    taskProgress.dec()
                    try {
                        task.executor(taskProgress)
                        synchronized(changed) { changed = true }
                        done += task.identifier
                        taskProgress.dec()
                    } catch (exception: Throwable) {
                        if (exception !is InterruptedException) {
                            exception.printStackTrace()
                        }
                        synchronized(changed) { changed = true }
                        if (task.optional) {
                            taskProgress.count = 0
                            errorHandler(task, exception)
                        } else {
                            criticalErrorHandler(task, exception)
                            exit = true
                        }
                    }
                }
            }
            if (exit) {
                break
            }
            if (synchronized(changed) { changed }) {
                continue
            }
            if (todo.isEmpty()) {
                break
            }
            workerProgress.waitForChange()
            if (exit) {
                break
            }
        }

        workerProgress.dec()
    }
}
