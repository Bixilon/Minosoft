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

package de.bixilon.minosoft.game.datatypes;

public enum LevelType {
    DEFAULT("default"),
    FLAT("flat"),
    LARGE_BIOMES("largeBiomes"),
    AMPLIFIED("amplified"),
    DEFAULT_1_1("default_1_1"),
    CUSTOMIZED("customized"),
    BUFFET("buffet"),
    UNKNOWN("unknown");

    final String type;

    LevelType(String type) {
        this.type = type;
    }

    public static LevelType byType(String type) {
        for (LevelType g : values()) {
            if (g.getId().equals(type)) {
                return g;
            }
        }
        return null;
    }

    public String getId() {
        return type;
    }
}
