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

public enum Color {
    WHITE(0x00),
    ORANGE(0x01),
    MAGENTA(0x02),
    LIGHT_BLUE(0x03),
    YELLOW(0x04),
    LIME(0x05),
    PINK(0x06),
    GRAY(0x07),
    SILVER(0x08),
    CYAN(0x09),
    PURPLE(0x0A),
    BLUE(0x0B),
    BROWN(0x0C),
    GREEN(0x0D),
    RED(0x0E),
    BLACK(0x0F);


    final int color;

    Color(int color) {
        this.color = color;
    }

    public static Color byId(int id) {
        for (Color c : values()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public int getId() {
        return color;
    }

}
