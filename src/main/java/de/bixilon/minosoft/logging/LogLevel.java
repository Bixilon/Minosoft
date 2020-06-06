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

public enum LogLevel {
    GAME(0),
    FATAL(1),
    INFO(2),
    WARNING(3),
    DEBUG(4),
    VERBOSE(5),
    PROTOCOL(6);

    private final int id;

    LogLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LogLevel byId(int id) {
        for (LogLevel g : values()) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    public static LogLevel byName(String name) {
        for (LogLevel g : values()) {
            if (g.name().equals(name)) {
                return g;
            }
        }
        return null;
    }
}
