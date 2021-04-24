/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.concurrent.LinkedBlockingQueue


object Log {
    private val MINOSOFT_START_TIME = System.currentTimeMillis()
    private val TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    private val LOG_QUEUE = LinkedBlockingQueue<MessageToSend>()
    private val SYSTEM_ERR_STREAM = System.err
    private val SYSTEM_OUT_STREAM = System.out
    private val ERROR_PRINT_STREAM: PrintStream = LogPrintStream(LogMessageType.OTHER_ERROR)
    private val OUT_PRINT_STREAM: PrintStream = LogPrintStream(LogMessageType.OTHER_INFO)


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
                    message.parts.add(TextComponent("[${TIME_FORMAT.format(messageToSend.time)}] "))
                    message.parts.add(TextComponent("[${messageToSend.thread.name}] "))
                    message.parts.add(TextComponent("[${messageToSend.logMessageType}] "))
                    messageToSend.additionalPrefix?.let {
                        message.parts.add(it)
                    }
                    message.parts.add(messageToSend.message)
                    message.applyDefaultColor(messageToSend.logMessageType.color)

                    val stream = if (messageToSend.logMessageType.error) {
                        SYSTEM_ERR_STREAM
                    } else {
                        SYSTEM_OUT_STREAM
                    }

                    stream.println(message.ansiColoredMessage)
                } catch (exception: Throwable) {
                    SYSTEM_ERR_STREAM.println("Can not send log message $messageToSend!")
                }
            }
        }, "Log").start()
    }

    @DoNotCall
    @JvmOverloads
    @JvmStatic
    fun log(logMessageType: LogMessageType, additionalPrefix: ChatComponent? = null, message: Any, vararg formatting: Any) {
        if (Minosoft.config != null && !Minosoft.config.config.general.enabledLogTypes.contains(logMessageType)) {
            return
        }
        val formattedMessage = when (message) {
            is ChatComponent -> message
            is Throwable -> {
                val stringWriter = StringWriter()
                message.printStackTrace(PrintWriter(stringWriter))
                ChatComponent.valueOf(raw = stringWriter.toString())
            }
            is String -> ChatComponent.valueOf(raw = message.format(*formatting))
            else -> ChatComponent.valueOf(raw = message)
        }

        LOG_QUEUE.add(
            MessageToSend(
                message = formattedMessage,
                time = System.currentTimeMillis(),
                logMessageType = logMessageType,
                thread = Thread.currentThread(),
                additionalPrefix = additionalPrefix,
            )
        )
    }

    @JvmStatic
    fun log(logMessageType: LogMessageType, additionalPrefix: ChatComponent? = null, messageBuilder: () -> Any) {
        if (!Minosoft.config.config.general.enabledLogTypes.contains(logMessageType)) {
            return
        }
        log(logMessageType, additionalPrefix, messageBuilder.invoke())
    }

    @JvmStatic
    fun log(logMessageType: LogMessageType, messageBuilder: () -> Any) {
        log(logMessageType, additionalPrefix = null, messageBuilder = messageBuilder)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(logMessageType, message = exception)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun printException(exception: Throwable, logMessageType: LogMessageType) {
        log(logMessageType, message = exception)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_FATAL, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun fatal(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_FATAL, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_ERROR, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun error(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_ERROR, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun info(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_DEBUG, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun debug(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_DEBUG, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_DEBUG, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun verbose(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_DEBUG, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.NETWORK_PACKETS_IN, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun protocol(message: Any, vararg formatting: Any) {
        log(LogMessageType.NETWORK_PACKETS_IN, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_ERROR, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun warn(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_ERROR, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun game(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)
    }

    @Deprecated(message = "Java only", replaceWith = ReplaceWith("log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)", "de.bixilon.minosoft.util.logging.Log.log"))
    @JvmStatic
    fun mojang(message: Any, vararg formatting: Any) {
        log(LogMessageType.OTHER_INFO, message = message, formatting = formatting)
    }
}
