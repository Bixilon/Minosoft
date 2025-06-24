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
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.div
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat


object FreezeDumpUtil {
    private var id = 0

    fun catchAsync(callback: (FreezeDump) -> Unit) {
        val thread = Thread { catch(callback) }
        thread.name = "FreezeDump#${id++}"
        thread.start()
    }

    fun catch(callback: (FreezeDump) -> Unit) {
        val builder = StringBuilder()

        builder.append("--- Freeze dump ----")
        builder.appendLine()
        builder.appendLine()
        builder.appendLine()
        ignoreAll {
            builder.append("-- Thread dump --")
            builder.appendLine()
            builder.append(createThreadDump())
            builder.appendLine()
            builder.appendLine()
        }
        ignoreAll {
            builder.append("-- Pool --")
            builder.appendLine()
            builder.append(createThreadPoolDump())
            builder.appendLine()
            builder.appendLine()
        }
        ignoreAll {
            builder.append("-- Scheduler --")
            builder.appendLine()
            builder.append(createSchedulerDump())
            builder.appendLine()
            builder.appendLine()
        }
        ignoreAll {
            builder.append("-- Locks --")
            builder.appendLine()
            builder.append(createLockDump())
            builder.appendLine()
            builder.appendLine()
        }


        val dump = builder.toString()


        var path: String?
        try {
            val crashReportFolder = (RunConfiguration.HOME_DIRECTORY / "dumps" / "freeze").toFile()
            crashReportFolder.mkdirs()

            path = "${crashReportFolder.slashPath}/freeze-${SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(TimeUtil.now())}.txt"

            val stream = FileOutputStream(path)

            stream.write(dump.toByteArray(StandardCharsets.UTF_8))
            stream.close()
        } catch (exception: Throwable) {
            exception.printStackTrace()
            path = null
        }

        callback(FreezeDump(dump, path))
    }

    private fun createThreadDump(): StringBuffer {
        val dump = StringBuffer(System.lineSeparator())
        val threadMXBean = ManagementFactory.getThreadMXBean()
        for (threadInfo in threadMXBean.dumpAllThreads(true, true)) {
            dump.append(threadInfo.toString()) // TODO: threadInfo.toString() is limited to 8 frames!
        }

        return dump
    }

    private fun createThreadPoolDump(): String {
        val queue = ThreadPool::class.java.getFieldOrNull("queue")!!
        return queue.get(DefaultThreadPool).toString()
    }

    private fun createSchedulerDump(): String {
        val tasks = TaskScheduler::class.java.getFieldOrNull("tasks")!!

        return tasks.get(TaskScheduler).toString()
    }

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
