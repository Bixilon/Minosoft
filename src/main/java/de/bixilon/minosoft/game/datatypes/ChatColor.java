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

public enum ChatColor {
    BLACK(0x00),
    DARK_BLUE(0x01),
    DARK_GREEN(0x02),
    DARK_AQUA(0x03),
    DARK_RED(0x04),
    DARK_PURPLE(0x05),
    GOLD(0x06),
    GRAY(0x07),
    DARK_GRAY(0x08),
    BLUE(0x09),
    GREEN(0x0A),
    AQUA(0x0B),
    RED(0x0C),
    PURPLE(0x0D),
    YELLOW(0x0E),
    WHITE(0x0F),
    OBFUSCATED(16),
    BOLD(17),
    STRIKETHROUGH(18),
    UNDERLINED(19),
    ITALIC(20),
    RESET(21);


    final int color;
    final String prefix;

    ChatColor(int color) {
        this.color = color;
        this.prefix = String.format("%x", color);
    }

    public static ChatColor byId(int id) {
        for (ChatColor c : values()) {
            if (c.getColor() == id) {
                return c;
            }
        }
        return null;
    }

    public int getColor() {
        return color;
    }

    public String getPrefix() {
        return prefix;
    }
}
