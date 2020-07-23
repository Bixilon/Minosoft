/*
 * Codename Minosoft
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
import de.bixilon.minosoft.game.datatypes.TextComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Log {
    final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    final static List<String> queue = new ArrayList<>();
    static LogLevel level = LogLevel.PROTOCOL;
    static Thread logThread;

    public static void log(LogLevel l, String message, TextComponent.ChatAttributes color) {
        if (l.getId() > level.getId()) {
            // log level too low
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(timeFormat.format(System.currentTimeMillis()));
        builder.append("] [");
        builder.append(l.name());
        builder.append("] ");
        if (color != null && Config.colorLog) {
            builder.append(color);
            builder.append(message);
            builder.append(TextComponent.ChatAttributes.RESET);
        } else {
            builder.append(message);
        }
        queue.add(builder.toString());

        logThread.interrupt();
    }

    public static void initThread() {
        logThread = new Thread(() -> {
            while (true) {
                while (queue.size() > 0) {
                    // something to print
                    System.out.println(queue.get(0));

                    // ToDo: log to file

                    queue.remove(0);
                }
                try {
                    // wait for interrupt
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        });
        logThread.setName("Log-Thread");
        logThread.start();
    }

    /**
     * Logs all game related things (chunk loading, rendering, ...)
     *
     * @param message Raw message to log
     */
    public static void game(String message) {
        log(LogLevel.GAME, message, TextComponent.ChatAttributes.GREEN);
    }

    /**
     * Logs all fatal errors (critical exceptions, etc)
     *
     * @param message Raw message to log
     */
    public static void fatal(String message) {
        log(LogLevel.FATAL, message, TextComponent.ChatAttributes.DARK_RED);
    }

    /**
     * Logs all general infos (connecting to server, ...)
     *
     * @param message Raw message to log
     */
    public static void info(String message) {
        log(LogLevel.INFO, message, TextComponent.ChatAttributes.WHITE);
    }

    /**
     * Logs all warnings (connection to server failed, ...)
     *
     * @param message Raw message to log
     */
    public static void warn(String message) {
        log(LogLevel.WARNING, message, TextComponent.ChatAttributes.RED);
    }

    /**
     * Logs all debug relevant infos (...)
     *
     * @param message Raw message to log
     */
    public static void debug(String message) {
        log(LogLevel.DEBUG, message, TextComponent.ChatAttributes.GRAY);
    }

    /**
     * Logs all debug relevant infos (even higher level!) (connection status, ...)
     *
     * @param message Raw message to log
     */
    public static void verbose(String message) {
        log(LogLevel.VERBOSE, message, TextComponent.ChatAttributes.YELLOW);
    }

    /**
     * Logs all protocol data (received protocol with length and command x,...)
     *
     * @param message Raw message to log
     */
    public static void protocol(String message) {
        log(LogLevel.PROTOCOL, message, TextComponent.ChatAttributes.BLUE);
    }

    /**
     * Logs all infos (mostly warnings) from data transfer to the mojang api (failed to login, etc)
     *
     * @param message Raw message to log
     */
    public static void mojang(String message) {
        log(LogLevel.MOJANG, message, TextComponent.ChatAttributes.AQUA);
    }

    public static LogLevel getLevel() {
        return level;
    }

    public static void setLevel(LogLevel level) {
        Log.level = level;
    }
}
