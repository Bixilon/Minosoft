/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.logging;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.ChatFormattingCodes;
import de.bixilon.minosoft.data.text.RGBColor;

import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;

public class Log {
    final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    final static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    final static long startTime = System.currentTimeMillis();
    static LogLevels level = LogLevels.PROTOCOL;

    public static void initThread() {
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

                // ToDo: log to file
            }
        }, "Log").start();
    }

    /**
     * Logs all game related things (mostly visible stuff to the user)
     *
     * @param message Raw message to log
     */
    public static void game(String message) {
        log(LogLevels.GAME, message, ChatColors.getColorByName("green"));
    }

    public static void log(LogLevels level, String message, RGBColor color) {
        log(level, "", message, color);
    }

    public static void log(LogLevels level, String prefix, String message, RGBColor color) {
        if (level.ordinal() > Log.level.ordinal()) {
            // log level too low
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (Config.logRelativeTime) {
            builder.append(System.currentTimeMillis() - startTime);
        } else {
            builder.append(timeFormat.format(System.currentTimeMillis()));
        }
        builder.append("] [");
        builder.append(Thread.currentThread().getName());
        builder.append("] [");
        builder.append(level.name());
        builder.append("] ");
        builder.append(prefix);
        if (color != null && Config.colorLog) {
            builder.append(ChatColors.getANSIColorByRGBColor(color));
            builder.append(message);
            builder.append(ChatFormattingCodes.RESET.getANSI());
        } else {
            builder.append(message);
        }
        builder.append(ChatFormattingCodes.RESET.getANSI());
        queue.add(builder.toString());
    }

    /**
     * Logs all fatal errors (critical exceptions, etc)
     *
     * @param message Raw message to log
     */
    public static void fatal(String message) {
        log(LogLevels.FATAL, message, ChatColors.getColorByName("dark_red"));
    }

    /**
     * Logs all warnings (error occurrence, ...)
     *
     * @param message Raw message to log
     */
    public static void warn(String message) {
        log(LogLevels.WARNING, message, ChatColors.getColorByName("red"));
    }

    /**
     * Logs way more data (data that might be important for resolving issues)
     *
     * @param message Raw message to log
     */
    public static void debug(String message) {
        log(LogLevels.DEBUG, message, ChatColors.getColorByName("gray"));
    }

    /**
     * Logs all debug relevant infos (even higher level!) (connection status, ...). Basically everything that happens
     *
     * @param message Raw message to log
     */
    public static void verbose(String message) {
        log(LogLevels.VERBOSE, message, ChatColors.getColorByName("yellow"));
    }

    /**
     * Logs all protocol data (received packet x with data, etc). Should only be used in packets
     *
     * @param message Raw message to log
     */
    public static void protocol(String message) {
        log(LogLevels.PROTOCOL, message, ChatColors.getColorByName("blue"));
    }

    /**
     * Logs all infos (mostly warnings) from data transfer to the mojang api (failed to login, etc)
     *
     * @param message Raw message to log
     */
    public static void mojang(String message) {
        log(LogLevels.MOJANG, message, ChatColors.getColorByName("aqua"));
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

    /**
     * Logs all general infos, that are more or less important to the user (connecting to server, ...)
     *
     * @param message Raw message to log
     */
    public static void info(String message) {
        log(LogLevels.INFO, message, ChatColors.getColorByName("white"));
    }
}
