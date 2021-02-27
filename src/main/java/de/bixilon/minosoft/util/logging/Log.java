/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.logging;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.ChatTextPositions;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.util.Pair;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;

public class Log {
    public static final long MINOSOFT_START_TIME = System.currentTimeMillis();
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final LinkedBlockingQueue<Pair<ChatComponent, ChatComponent>> LOG_QUEUE = new LinkedBlockingQueue<>(); // prefix, message
    private static final PrintStream SYSTEM_ERR_STREAM = System.err;
    private static final PrintStream SYSTEM_OUT_STREAM = System.out;
    private static final PrintStream ERROR_PRINT_STREAM = new LogPrintStream(LogLevels.WARNING);
    private static final PrintStream OUT_PRINT_STREAM = new LogPrintStream(LogLevels.INFO);
    private static LogLevels level = LogLevels.PROTOCOL;

    static {
        System.setErr(ERROR_PRINT_STREAM);
        System.setOut(OUT_PRINT_STREAM);
        new Thread(() -> {
            while (true) {
                // something to print
                Pair<ChatComponent, ChatComponent> message;
                try {
                    message = LOG_QUEUE.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                SYSTEM_OUT_STREAM.println(message.getKey().getANSIColoredMessage() + message.getValue().getANSIColoredMessage());

                if (StaticConfiguration.SHOW_LOG_MESSAGES_IN_CHAT) {
                    for (var connection : Minosoft.CONNECTIONS.values()) {
                        connection.getSender().sendFakeChatMessage(message.getValue(), ChatTextPositions.CHAT_BOX);
                    }
                }
                // ToDo: log to file
            }
        }, "Log").start();
    }

    public static void log(LogLevels level, RGBColor color, Object message, Object... format) {
        log(level, "", color, message, format);
    }

    public static void log(LogLevels level, Object message, Object... format) {
        log(level, "", switch (level) {
            case GAME -> ChatColors.GREEN;
            case FATAL -> ChatColors.DARK_RED;
            case WARNING -> ChatColors.RED;
            case DEBUG -> ChatColors.GRAY;
            case VERBOSE -> ChatColors.YELLOW;
            case PROTOCOL -> ChatColors.BLUE;
            case MOJANG -> ChatColors.AQUA;
            case INFO -> ChatColors.WHITE;
        }, message, format);
    }

    public static void log(LogLevels level, String prefix, RGBColor color, Object message, Object... format) {
        if (level.ordinal() > Log.level.ordinal()) {
            // log level too low
            return;
        }
        if (message == null) {
            return;
        }
        if (message instanceof String string) {
            if (string.isBlank()) {
                return;
            }
            if (format.length > 0) {
                message = String.format(string, format);
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (StaticConfiguration.LOG_RELATIVE_TIME) {
            builder.append(System.currentTimeMillis() - MINOSOFT_START_TIME);
        } else {
            builder.append(TIME_FORMAT.format(System.currentTimeMillis()));
        }
        builder.append("] [");
        builder.append(Thread.currentThread().getName());
        builder.append("] [");
        builder.append(level.name());
        builder.append("] ");
        builder.append(prefix);
        var component = (BaseComponent) ChatComponent.valueOf(builder.toString());
        var messageComponent = (BaseComponent) ChatComponent.valueOf(message);
        if (color != null && StaticConfiguration.COLORED_LOG) {
            messageComponent.applyDefaultColor(color);
        }
        LOG_QUEUE.add(new Pair<>(component, messageComponent));
    }

    /**
     * Logs all game related things (mostly visible stuff to the user)
     *
     * @param message Raw message to log
     */
    public static void game(Object message, Object... format) {
        log(LogLevels.GAME, message, format);
    }

    /**
     * Logs all fatal errors (critical exceptions, etc)
     *
     * @param message Raw message to log
     */
    public static void fatal(Object message, Object... format) {
        log(LogLevels.FATAL, message, format);
    }

    /**
     * Logs all warnings (error occurrence, ...)
     *
     * @param message Raw message to log
     */
    public static void warn(Object message, Object... format) {
        log(LogLevels.WARNING, message, format);
    }

    /**
     * Logs way more data (data that might be important for resolving issues)
     *
     * @param message Raw message to log
     */
    public static void debug(Object message, Object... format) {
        log(LogLevels.DEBUG, message, format);
    }

    /**
     * Logs all debug relevant infos (even higher level!) (connection status, ...). Basically everything that happens
     *
     * @param message Raw message to log
     */
    public static void verbose(Object message, Object... format) {
        log(LogLevels.VERBOSE, message, format);
    }

    /**
     * Logs all protocol data (received packet x with data, etc). Should only be used in packets
     *
     * @param message Raw message to log
     */
    public static void protocol(Object message, Object... format) {
        log(LogLevels.PROTOCOL, message, format);
    }

    /**
     * Logs all infos (mostly warnings) from data transfer to the mojang api (failed to login, etc)
     *
     * @param message Raw message to log
     */
    public static void mojang(Object message, Object... format) {
        log(LogLevels.MOJANG, message, format);
    }

    /**
     * Logs all general infos, that are more or less important to the user (connecting to server, ...)
     *
     * @param message Raw message to log
     */
    public static void info(Object message, Object... format) {
        log(LogLevels.INFO, message, format);
    }

    public static LogLevels getLevel() {
        return level;
    }

    public static void setLevel(LogLevels level) {
        if (Log.level == level) {
            return;
        }
        info(String.format("Log level changed from %s to %s", Log.level, level));
        Log.level = level;
    }

    public static boolean printException(Throwable exception, LogLevels minimumLogLevel) {
        // ToDo: log to file, print also exceptions that are not printed with this method
        if (getLevel().ordinal() >= minimumLogLevel.ordinal()) {
            exception.printStackTrace();
            return true;
        }
        return false;
    }

    public static boolean printException(Exception exception) {
        return printException(exception, LogLevels.FATAL); // always print
    }
}
