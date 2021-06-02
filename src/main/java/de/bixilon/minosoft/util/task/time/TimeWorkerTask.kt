package de.bixilon.minosoft.util.task.time

import java.util.concurrent.locks.ReentrantLock

data class TimeWorkerTask(
    val interval: Int,
    val runOnce: Boolean = false,
    val maxDelayTime: Int = Int.MAX_VALUE,
    val runnable: Runnable,
) {
    val lock = ReentrantLock()
    var getsExecuted: Boolean = false
    var lastExecution: Long = 0L
}
