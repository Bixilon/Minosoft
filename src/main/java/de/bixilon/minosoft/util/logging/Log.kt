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
package de.bixilon.minosoft.util.logging

import de.bixilon.kutil.ansi.ANSI.RESET
import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.format1
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


object Log {
    var ASYNC_LOGGING = true
    private val MINOSOFT_START_TIME = now()
    private val TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    private val QUEUE = LinkedBlockingQueue<QueuedMessage>()
    private val SYSTEM_ERR_STREAM = System.err
    private val SYSTEM_OUT_STREAM = System.out
    private var levels: Map<LogMessageType, LogLevels>? = null

    val FATAL_PRINT_STREAM: PrintStream = LogPrintStream(LogMessageType.OTHER, LogLevels.FATAL)
    val ERROR_PRINT_STREAM: PrintStream = LogPrintStream(LogMessageType.OTHER, LogLevels.WARN)
    val OUT_PRINT_STREAM: PrintStream = LogPrintStream(LogMessageType.OTHER, LogLevels.INFO)


    init {
        if (StaticConfiguration.REPLACE_SYSTEM_OUT_STREAMS) {
            System.setErr(ERROR_PRINT_STREAM)
            System.setOut(OUT_PRINT_STREAM)
        }
        Thread({
            while (true) {
                QUEUE.take().print()
            }
        }, "Log").start()
        ShutdownManager.addHook { ASYNC_LOGGING = false; catchAll { await() } }
    }

    @OptIn(ExperimentalTime::class)
    private fun QueuedMessage.print() {
        try {
            val message = BaseComponent()
            val color = this.type.colorMap[this.level] ?: this.type.defaultColor
            message += if (RunConfiguration.LOG_RELATIVE_TIME) {
                TextComponent("[${now() - MINOSOFT_START_TIME}] ")
            } else {
                TextComponent("[${TIME_FORMAT.format1(this.time)}] ")
            }
            message += TextComponent("[${this.thread.name}] ")
            message += TextComponent("[${this.type}] ").let { if (RunConfiguration.LOG_COLOR_TYPE) it.color(color) else it }
            message += TextComponent("[${this.level}] ").let { if (RunConfiguration.LOG_COLOR_LEVEL) it.color(this.level.levelColors) else it }
            this.prefix?.let { message += it }
            if (RunConfiguration.LOG_COLOR_MESSAGE) {
                this.message.setFallbackColor(color)
            }

            val stream = if (this.level.error) SYSTEM_ERR_STREAM else SYSTEM_OUT_STREAM

            val prefix = message.ansi.removeSuffix(RESET) // reset suffix
            for (line in this.message.ansi.lineSequence()) {
                stream.println(prefix + line + RESET)
            }
            stream.flush()

        } catch (exception: Throwable) {
            SYSTEM_ERR_STREAM.println("Can not send log message $this: $exception!")
        }
    }


    fun skipLogging(type: LogMessageType, level: LogLevels): Boolean {
        if (RunConfiguration.VERBOSE_LOGGING) {
            return false
        }
        val setLevel = levels?.get(type) ?: LogLevels.INFO
        return setLevel.ordinal < level.ordinal
    }

    private fun formatMessage(message: Any?, vararg formatting: Any) = when (message) {
        is ChatComponent -> message
        is TextFormattable -> ChatComponent.of(message.toText())
        is Throwable -> {
            val stringWriter = StringWriter()
            message.printStackTrace(PrintWriter(stringWriter))
            ChatComponent.of(stringWriter.toString(), ignoreJson = true)
        }

        is String -> when {
            message.isBlank() -> ChatComponent.EMPTY
            formatting.isNotEmpty() -> ChatComponent.of(message.format(*formatting), ignoreJson = true)
            else -> ChatComponent.of(message, ignoreJson = true)
        }

        else -> ChatComponent.of(message, ignoreJson = true)
    }

    @JvmStatic
    @OptIn(ExperimentalTime::class)
    fun logInternal(type: LogMessageType, level: LogLevels, prefix: ChatComponent?, message: Any?, vararg formatting: Any) {
        val formatted = formatMessage(message, *formatting)
        if (formatted.length <= 0) return

        QueuedMessage(message = formatted, time = Clock.System.now(), type = type, level = level, thread = Thread.currentThread(), prefix = prefix).queue()
    }

    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels, prefix: ChatComponent?, message: Any?, vararg formatting: Any) {
        if (skipLogging(type, level)) return
        logInternal(type, level, prefix, message, formatting)
    }

    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels, prefix: ChatComponent?, message: Any?) {
        if (skipLogging(type, level)) return
        logInternal(type, level, prefix, message)
    }

    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels, message: Any?) {
        if (skipLogging(type, level)) return
        logInternal(type, level, null, message)
    }

    inline fun log(type: LogMessageType, level: LogLevels, prefix: ChatComponent?, builder: () -> Any?, vararg formatting: Any) {
        if (skipLogging(type, level)) return
        logInternal(type, level, prefix, builder.invoke(), *formatting)
    }

    inline fun log(type: LogMessageType, level: LogLevels, prefix: ChatComponent?, builder: () -> Any?) {
        if (skipLogging(type, level)) return
        logInternal(type, level, prefix, builder.invoke())
    }

    inline fun log(type: LogMessageType, level: LogLevels, builder: () -> Any?) {
        if (skipLogging(type, level)) return
        logInternal(type, level, null, builder.invoke())
    }

    inline fun log(type: LogMessageType, builder: () -> Any?) {
        if (skipLogging(type, LogLevels.INFO)) return
        logInternal(type, LogLevels.INFO, null, builder.invoke())
    }


    private fun QueuedMessage.queue() {
        if (!ASYNC_LOGGING) {
            this.print()
            return
        }
        QUEUE += this
    }

    fun await() {
        while (this.QUEUE.isNotEmpty()) {
            Thread.sleep(1)
        }
    }

    fun init() = Unit

    fun observeProfile() {
        OtherProfileManager::selected.observe(this, true) { this.levels = it.log.levels }
    }
}
