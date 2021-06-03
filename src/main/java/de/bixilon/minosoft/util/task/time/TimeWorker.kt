package de.bixilon.minosoft.util.task.time

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import java.util.concurrent.TimeUnit

object TimeWorker {
    private val TASKS: MutableSet<TimeWorkerTask> = synchronizedSetOf()

    init {
        Thread({
            while (true) {
                val currentTime = System.currentTimeMillis()
                for (task in TASKS.toSynchronizedSet()) {
                    if (task.getsExecuted) {
                        continue
                    }
                    if (currentTime - task.lastExecution <= task.interval) {
                        continue
                    }
                    Minosoft.THREAD_POOL.execute {
                        if (!task.lock.tryLock(100L, TimeUnit.MILLISECONDS)) {
                            return@execute
                        }
                        if (task.getsExecuted) {
                            task.lock.unlock()
                            return@execute
                        }
                        if (System.currentTimeMillis() - currentTime >= task.maxDelayTime) {
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
        task.lastExecution = System.currentTimeMillis()
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
