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

    public void log(LogLevels level, RGBColor color, Object message, Object... format) {
        Log.log(level, String.format("[%s] ", this.modName), color, message, format);
    }

    public void game(Object message, Object... format) {
        log(LogLevels.GAME, ChatColors.GREEN, message, format);
    }

    public void fatal(Object message, Object... format) {
        log(LogLevels.FATAL, ChatColors.DARK_RED, message, format);
    }

    public void info(Object message, Object... format) {
        log(LogLevels.INFO, ChatColors.WHITE, message, format);
    }

    public void warn(Object message, Object... format) {
        log(LogLevels.WARNING, ChatColors.RED, message, format);
    }

    public void debug(Object message, Object... format) {
        log(LogLevels.DEBUG, ChatColors.GRAY, message, format);
    }

    public void verbose(Object message, Object... format) {
        log(LogLevels.VERBOSE, ChatColors.YELLOW, message, format);
    }
}
