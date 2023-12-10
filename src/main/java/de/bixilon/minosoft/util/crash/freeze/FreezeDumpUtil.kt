/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ThreadPoolRunnable
import de.bixilon.kutil.file.FileUtil.slashPath
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.terminal.RunConfiguration
import java.io.FileOutputStream
import java.lang.management.ManagementFactory
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.concurrent.PriorityBlockingQueue


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
        builder.append("-- Thread dump --")
        builder.appendLine()
        builder.append(createThreadDump())
        builder.appendLine()
        builder.append("-- Pool --")
        builder.appendLine()
        builder.append(createThreadPoolDump())


        val dump = builder.toString()


        var path: String?
        try {
            val crashReportFolder = RunConfiguration.HOME_DIRECTORY.resolve("dumps").resolve("freeze").toFile()
            crashReportFolder.mkdirs()

            path = "${crashReportFolder.slashPath}/freeze-${SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(TimeUtil.millis())}.txt"

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
            dump.append(threadInfo.toString())
        }

        return dump
    }

    private fun createThreadPoolDump(): String {
        val tasks = ThreadPool::class.java.getFieldOrNull("queue")!!

        val queue = tasks.get(DefaultThreadPool) as PriorityBlockingQueue<ThreadPoolRunnable>

        return queue.toString()
    }
}
