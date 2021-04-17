package de.bixilon.minosoft.util.time

data class TimeWorkerTask(
    val interval: Int,
    val runnable: Runnable,
) {
    var lastExecution: Long = 0L
}
