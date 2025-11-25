/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.crash.freeze

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.io.DefaultIOPool
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.file.FileUtil.div
import de.bixilon.kutil.file.PathUtil.div
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.reflection.ReflectionUtil.static
import de.bixilon.kutil.time.TimeUtil.format1
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.terminal.RunConfiguration
import java.io.File
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


object FreezeDumpUtil {
    private val SCHEDULER_REPEATED = TaskScheduler::class.java.getFieldOrNull("repeated")!!.static
    private val SCHEDULER_QUEUED = TaskScheduler::class.java.getFieldOrNull("queued")!!.static
    private val THREAD_POOL_QUEUE = ThreadPool::class.java.getFieldOrNull("queue")!!.field

    private var id = 0


    private val SECTIONS = listOf(
        "Thread dump" to this::createThreadDump,
        "Thread Pool" to this::createThreadPoolDump,
        "IO Pool" to this::createIOPoolDump,
        "Scheduler Queued" to this::createSchedulerDumpQueued,
        "Scheduler Repeated" to this::createSchedulerDumpRepeated,
        "Locks" to this::createLockDump,
    )

    fun catchAsync(callback: (FreezeDump) -> Unit) {
        val thread = Thread {
            val dump = catch()
            callback.invoke(dump)
        }
        thread.name = "FreezeDump#${id++}"
        thread.start()
    }

    fun dump(): String {
        val builder = StringBuilder()


        builder.append("--- Freeze dump ----")
        builder.appendLine()
        builder.appendLine()
        builder.appendLine()

        for ((title, runnable) in SECTIONS) {
            val data = ignoreAll { runnable.invoke() } ?: continue
            builder.append("-- ").append(title).append(" --")
            builder.appendLine()
            builder.append(data)
            builder.appendLine()
            builder.appendLine()
        }

        return builder.toString()
    }

    @OptIn(ExperimentalTime::class)
    fun catch(): FreezeDump {
        val dump = dump()

        var path: File?
        try {
            val crashReportFolder = (RunConfiguration.home / "dumps" / "freeze").toFile()
            crashReportFolder.mkdirs()

            path = crashReportFolder / "freeze-${SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format1(Clock.System.now())}.txt"

            val stream = FileOutputStream(path)

            stream.write(dump.toByteArray(StandardCharsets.UTF_8))
            stream.close()
        } catch (exception: Throwable) {
            exception.printStackTrace()
            path = null
        }

        return FreezeDump(dump, path)
    }

    private fun createThreadDump(): StringBuffer {
        val dump = StringBuffer(System.lineSeparator())
        val threadMXBean = ManagementFactory.getThreadMXBean()
        for (threadInfo in threadMXBean.dumpAllThreads(true, true)) {
            dump.append(threadInfo.toString()) // TODO: threadInfo.toString() is limited to 8 frames!
        }

        return dump
    }

    private fun createThreadPoolDump() = THREAD_POOL_QUEUE.getAny(DefaultThreadPool).toString()
    private fun createIOPoolDump() = THREAD_POOL_QUEUE.getAny(DefaultIOPool).toString()
    private fun createSchedulerDumpQueued() = SCHEDULER_QUEUED.getAny().toString()
    private fun createSchedulerDumpRepeated() = SCHEDULER_REPEATED.getAny().toString()

    private fun RWLock.owner() = when (this) {
        is ReentrantRWLock -> owner()
        else -> "unknown"
    }

    private fun ReentrantRWLock.owner() = this::class.java.getFieldOrNull("lock")?.field?.get<Any>(this).toString()

    private fun createLockDump(): String {
        val dump = StringBuffer()

        for (session in PlaySession.collectSessions()) {
            dump.appendLine()
            dump.append("#${session.id}\n")
            dump.append("    world: ${session.world.lock.owner()}\n")
            dump.append("    entities: ${session.world.entities.lock.owner()}\n")
        }
        return dump.toString()
    }
}
