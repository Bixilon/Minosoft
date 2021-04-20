package de.bixilon.minosoft.util.time

import de.bixilon.minosoft.Minosoft

object TimeWorker {
    private val TASKS: MutableList<TimeWorkerTask> = mutableListOf()

    init {
        Thread({
               while (true) {
                   val currentTime = System.currentTimeMillis()
                   synchronized(TASKS) {
                       for (task in TASKS) {
                           if (currentTime - task.lastExecution >= task.interval) {
                               Minosoft.THREAD_POOL.execute(task.runnable)
                               task.lastExecution = currentTime
                           }
                       }
                   }
                   Thread.sleep(1)
               }
        }, "TimeWorkerThread").start()
    }

    fun addTask(task: TimeWorkerTask) {
        synchronized(TASKS) {
            TASKS.add(task)
        }
    }

    fun removeTask(task: TimeWorkerTask) {
        synchronized(TASKS) {
            TASKS.remove(task)
        }
    }
}
