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

package de.bixilon.minosoft.modding;

import de.bixilon.minosoft.data.text.ChatColors;
import de.bixilon.minosoft.data.text.RGBColor;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;

public class Logger {
    private final String modName;

    public Logger(String modName) {
        this.modName = modName;
    }

    /**
     * Logs all game related things (chunk loading, rendering, ...)
     *
     * @param message Raw message to log
     */
    public void game(String message) {
        log(LogLevels.GAME, message, ChatColors.getColorByName("green"));
    }

    public void log(LogLevels level, String message, RGBColor color) {
        Log.log(level, String.format("[%s] ", modName), message, color);
    }

    /**
     * Logs all fatal errors (critical exceptions, etc)
     *
     * @param message Raw message to log
     */
    public void fatal(String message) {
        log(LogLevels.FATAL, message, ChatColors.getColorByName("dark_red"));
    }

    /**
     * Logs all general infos (connecting to server, ...)
     *
     * @param message Raw message to log
     */
    public void info(String message) {
        log(LogLevels.INFO, message, ChatColors.getColorByName("white"));
    }

    /**
     * Logs all warnings (connection to server failed, ...)
     *
     * @param message Raw message to log
     */
    public void warn(String message) {
        log(LogLevels.WARNING, message, ChatColors.getColorByName("red"));
    }

    /**
     * Logs all debug relevant infos (...)
     *
     * @param message Raw message to log
     */
    public void debug(String message) {
        log(LogLevels.DEBUG, message, ChatColors.getColorByName("gray"));
    }

    /**
     * Logs all debug relevant infos (even higher level!) (connection status, ...)
     *
     * @param message Raw message to log
     */
    public void verbose(String message) {
        log(LogLevels.VERBOSE, message, ChatColors.getColorByName("yellow"));
    }

    /**
     * Logs all protocol data (received protocol with length and command x,...)
     *
     * @param message Raw message to log
     */
    public void protocol(String message) {
        log(LogLevels.PROTOCOL, message, ChatColors.getColorByName("blue"));
    }
}
