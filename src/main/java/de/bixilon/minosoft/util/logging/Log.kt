/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import com.google.errorprone.annotations.DoNotCall
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileSelectEvent
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.terminal.RunConfiguration
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.LinkedBlockingQueue


object Log {
    private val MINOSOFT_START_TIME = TimeUtil.millis
    private val TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    private val LOG_QUEUE = LinkedBlockingQueue<MessageToSend>()
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
                val messageToSend = LOG_QUEUE.take()
                try {
                    val message = BaseComponent()
                    val messageColor = messageToSend.logMessageType.colorMap[messageToSend.level] ?: messageToSend.logMessageType.defaultColor
                    message += if (RunConfiguration.LOG_RELATIVE_TIME) {
                        TextComponent("[${TimeUtil.millis - MINOSOFT_START_TIME}] ")
                    } else {
                        TextComponent("[${TIME_FORMAT.format(messageToSend.time)}] ")
                    }
                    message += TextComponent("[${messageToSend.thread.name}] ")
                    message += TextComponent("[${messageToSend.logMessageType}] ").let {
                        if (RunConfiguration.LOG_COLOR_TYPE) {
                            it.color(messageColor)
                        } else {
                            it
                        }
                    }
                    message += TextComponent("[${messageToSend.level}] ").let {
                        if (RunConfiguration.LOG_COLOR_LEVEL) {
                            it.color(messageToSend.level.levelColors)
                        } else {
                            it
                        }
                    }
                    messageToSend.additionalPrefix?.let {
                        message += it
                    }
                    if (RunConfiguration.LOG_COLOR_MESSAGE) {
                        messageToSend.message.setFallbackColor(messageColor)
                    }

                    val stream = if (messageToSend.logMessageType.error) {
                        SYSTEM_ERR_STREAM
                    } else {
                        SYSTEM_OUT_STREAM
                    }

                    val prefix = message.ansiColoredMessage.removeSuffix("\u001b[0m") // reset suffix
                    for (line in messageToSend.message.ansiColoredMessage.lines()) {
                        stream.println(prefix + line)
                    }

                } catch (exception: Throwable) {
                    SYSTEM_ERR_STREAM.println("Can not send log message $messageToSend!")
                }
            }
        }, "Log").start()

        GlobalEventMaster.registerEvent(CallbackEventInvoker.of<OtherProfileSelectEvent> { this.levels = it.profile.log.levels })
    }


    private fun skipLogging(type: LogMessageType, level: LogLevels): Boolean {
        if (RunConfiguration.VERBOSE_LOGGING) {
            return false
        }
        val setLevel = levels?.get(type) ?: LogLevels.INFO
        if (setLevel.ordinal < level.ordinal) {
            return true
        }
        return false
    }

    @DoNotCall
    @JvmOverloads
    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels = LogLevels.INFO, additionalPrefix: ChatComponent? = null, message: Any, vararg formatting: Any) {
        if (skipLogging(type, level)) {
            return
        }
        val formattedMessage = when (message) {
            is ChatComponent -> message
            is Throwable -> {
                val stringWriter = StringWriter()
                message.printStackTrace(PrintWriter(stringWriter))
                ChatComponent.of(stringWriter.toString(), ignoreJson = true)
            }
            is String -> {
                if (message.isBlank()) {
                    return
                }
                if (formatting.isNotEmpty()) {
                    ChatComponent.of(message.format(*formatting), ignoreJson = true)
                } else {
                    ChatComponent.of(message, ignoreJson = true)
                }
            }
            else -> ChatComponent.of(message, ignoreJson = true)
        }

        LOG_QUEUE += MessageToSend(
            message = formattedMessage,
            time = TimeUtil.millis,
            logMessageType = type,
            level = level,
            thread = Thread.currentThread(),
            additionalPrefix = additionalPrefix,
        )
    }

    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels = LogLevels.INFO, additionalPrefix: ChatComponent? = null, messageBuilder: () -> Any) {
        if (skipLogging(type, level)) {
            return
        }
        log(type, level, additionalPrefix, messageBuilder())
    }

    @JvmStatic
    fun log(type: LogMessageType, level: LogLevels, messageBuilder: () -> Any) {
        log(type, level = level, additionalPrefix = null, messageBuilder = messageBuilder)
    }

    @JvmStatic
    fun log(type: LogMessageType, messageBuilder: () -> Any) {
        log(type, additionalPrefix = null, messageBuilder = messageBuilder)
    }
}
