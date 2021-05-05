package de.bixilon.minosoft.util.time

data class TimeWorkerTask(
    val interval: Int,
    val runOnce: Boolean = true,
    val runnable: Runnable,
) {
    var getsExecuted: Boolean = false
    var lastExecution: Long = 0L
}
