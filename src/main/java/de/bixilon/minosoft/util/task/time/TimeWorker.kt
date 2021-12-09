package de.bixilon.minosoft.util.task.time

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import java.util.concurrent.TimeUnit

object TimeWorker {
    private val TASKS: MutableSet<TimeWorkerTask> = synchronizedSetOf()

    init {
        Thread({
            while (true) {
                val currentTime = KUtil.time
                for (task in TASKS.toSynchronizedSet()) {
                    if (task.executing) {
                        continue
                    }
                    if (currentTime - task.lastExecution <= task.interval) {
                        continue
                    }
                    DefaultThreadPool += execute@{
                        if (!task.lock.tryLock(100L, TimeUnit.MILLISECONDS)) {
                            return@execute
                        }
                        if (task.executing) {
                            task.lock.unlock()
                            return@execute
                        }
                        if (KUtil.time - currentTime >= task.maxDelayTime) {
                            task.lock.unlock()
                            return@execute
                        }
                        task.executing = true
                        try {
                            task.runnable.run()
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                        task.lastExecution = currentTime
                        task.executing = false
                        task.lock.unlock()
                    }
                    if (task.runOnce) {
                        TASKS -= task
                    }
                }
                Thread.sleep(1)
            }
        }, "TimeWorkerThread").start()
    }

    fun runIn(millis: Int, runnable: Runnable) {
        val task = TimeWorkerTask(
            interval = millis,
            runnable = runnable,
            runOnce = true,
        )
        task.lastExecution = KUtil.time
        TASKS += task
    }

    fun addTask(task: TimeWorkerTask) {
        TASKS += task
    }

    operator fun plusAssign(task: TimeWorkerTask) = addTask(task)

    fun removeTask(task: TimeWorkerTask) {
        TASKS -= task
    }

    operator fun minusAssign(task: TimeWorkerTask) = removeTask(task)
}
