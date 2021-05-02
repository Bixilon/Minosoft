package de.bixilon.minosoft.util.time

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList

object TimeWorker {
    private val TASKS: MutableList<TimeWorkerTask> = synchronizedListOf()

    init {
        Thread({
            while (true) {
                val currentTime = System.currentTimeMillis()
                for (task in TASKS.toSynchronizedList()) {
                    if (currentTime - task.lastExecution >= task.interval) {
                        Minosoft.THREAD_POOL.execute(task.runnable)
                        task.lastExecution = currentTime
                    }
                }
                Thread.sleep(1)
            }
        }, "TimeWorkerThread").start()
    }

    fun addTask(task: TimeWorkerTask) {
        TASKS += task
    }

    fun removeTask(task: TimeWorkerTask) {
        TASKS -= task
    }
}
