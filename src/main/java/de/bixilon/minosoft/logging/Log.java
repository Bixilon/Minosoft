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

package de.bixilon.minosoft.logging;

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.PostChatFormattingCodes;
import de.bixilon.minosoft.data.text.RGBColor;

import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;

public class Log {
    public final static long MINOSOFT_START_TIME = System.currentTimeMillis();
    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static LogLevels level = LogLevels.PROTOCOL;

    static {
        new Thread(() -> {
            while (true) {
                // something to print
                String message;
                try {
                    message = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    continue;
                }
                System.out.println(message);
                System.out.flush();

                // ToDo: log to file
            }
        }, "Log").start();
    }

    public static void log(LogLevels level, RGBColor color, Object message, Object... format) {
        log(level, "", color, message, format);
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
            builder.append(timeFormat.format(System.currentTimeMillis()));
        }
        builder.append("] [");
        builder.append(Thread.currentThread().getName());
        builder.append("] [");
        builder.append(level.name());
        builder.append("] ");
        builder.append(prefix);
        if (color != null && StaticConfiguration.COLORED_LOG) {
            builder.append(ChatColors.getANSIColorByRGBColor(color));
            builder.append(message);
            builder.append(PostChatFormattingCodes.RESET.getANSI());
        } else {
            builder.append(message);
        }
        builder.append(PostChatFormattingCodes.RESET.getANSI());
        queue.add(builder.toString());
    }

    /**
     * Logs all game related things (mostly visible stuff to the user)
     *
     * @param message Raw message to log
     */
    public static void game(Object message, Object... format) {
        log(LogLevels.GAME, ChatColors.GREEN, message, format);
    }

    /**
     * Logs all fatal errors (critical exceptions, etc)
     *
     * @param message Raw message to log
     */
    public static void fatal(Object message, Object... format) {
        log(LogLevels.FATAL, ChatColors.DARK_RED, message, format);
    }

    /**
     * Logs all warnings (error occurrence, ...)
     *
     * @param message Raw message to log
     */
    public static void warn(Object message, Object... format) {
        log(LogLevels.WARNING, ChatColors.RED, message, format);
    }

    /**
     * Logs way more data (data that might be important for resolving issues)
     *
     * @param message Raw message to log
     */
    public static void debug(Object message, Object... format) {
        log(LogLevels.DEBUG, ChatColors.GRAY, message, format);
    }

    /**
     * Logs all debug relevant infos (even higher level!) (connection status, ...). Basically everything that happens
     *
     * @param message Raw message to log
     */
    public static void verbose(Object message, Object... format) {
        log(LogLevels.VERBOSE, ChatColors.YELLOW, message, format);
    }

    /**
     * Logs all protocol data (received packet x with data, etc). Should only be used in packets
     *
     * @param message Raw message to log
     */
    public static void protocol(Object message, Object... format) {
        log(LogLevels.PROTOCOL, ChatColors.BLUE, message, format);
    }

    /**
     * Logs all infos (mostly warnings) from data transfer to the mojang api (failed to login, etc)
     *
     * @param message Raw message to log
     */
    public static void mojang(Object message, Object... format) {
        log(LogLevels.MOJANG, ChatColors.AQUA, message, format);
    }

    /**
     * Logs all general infos, that are more or less important to the user (connecting to server, ...)
     *
     * @param message Raw message to log
     */
    public static void info(Object message, Object... format) {
        log(LogLevels.INFO, ChatColors.WHITE, message, format);
    }


    public static LogLevels getLevel() {
        return level;
    }

    public static void setLevel(LogLevels level) {
        if (Log.level == level) {
            return;
        }
        Log.info(String.format("Log level changed from %s to %s", Log.level, level));
        Log.level = level;
    }

    public static boolean printException(Throwable exception, LogLevels minimumLogLevel) {
        // ToDo: log to file, print also exceptions that are not printed with this method
        if (Log.getLevel().ordinal() >= minimumLogLevel.ordinal()) {
            exception.printStackTrace();
            return true;
        }
        return false;
    }

    public static boolean printException(Exception exception) {
        return printException(exception, LogLevels.FATAL); // always print
    }
}
