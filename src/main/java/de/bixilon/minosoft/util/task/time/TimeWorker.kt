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
                    if (task.getsExecuted) {
                        continue
                    }
                    if (currentTime - task.lastExecution <= task.interval) {
                        continue
                    }
                    DefaultThreadPool += execute@{
                        if (!task.lock.tryLock(100L, TimeUnit.MILLISECONDS)) {
                            return@execute
                        }
                        if (task.getsExecuted) {
                            task.lock.unlock()
                            return@execute
                        }
                        if (KUtil.time - currentTime >= task.maxDelayTime) {
                            task.lock.unlock()
                            return@execute
                        }
                        task.getsExecuted = true
                        task.runnable.run()
                        task.lastExecution = currentTime
                        task.getsExecuted = false
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

    fun addTask(task: TimeWorkerTask): TimeWorkerTask {
        TASKS += task
        return task
    }

    fun removeTask(task: TimeWorkerTask) {
        TASKS -= task
    }
}
