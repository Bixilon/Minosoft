package de.bixilon.minosoft.util.time

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet

object TimeWorker {
    private val TASKS: MutableSet<TimeWorkerTask> = synchronizedSetOf()

    init {
        Thread({
            while (true) {
                val currentTime = System.currentTimeMillis()
                for (task in TASKS.toSynchronizedSet()) {
                    if (!task.getsExecuted && currentTime - task.lastExecution >= task.interval) {
                        Minosoft.THREAD_POOL.execute {
                            task.getsExecuted = true
                            task.runnable.run()
                            task.lastExecution = currentTime
                            task.getsExecuted = false
                        }
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

    fun addTask(task: TimeWorkerTask) {
        TASKS += task
    }

    fun removeTask(task: TimeWorkerTask) {
        TASKS -= task
    }
}
