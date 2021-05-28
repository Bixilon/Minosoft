package de.bixilon.minosoft.util.task.time

data class TimeWorkerTask(
    val interval: Int,
    val runOnce: Boolean = false,
    val maxDelayTime: Int = Int.MAX_VALUE,
    val runnable: Runnable,
) {
    var getsExecuted: Boolean = false
    var lastExecution: Long = 0L
}
