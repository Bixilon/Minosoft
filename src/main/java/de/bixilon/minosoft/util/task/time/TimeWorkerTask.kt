package de.bixilon.minosoft.util.task.time

data class TimeWorkerTask(
    val interval: Int,
    val runOnce: Boolean = false,
    val runnable: Runnable,
) {
    var getsExecuted: Boolean = false
    var lastExecution: Long = 0L
}
