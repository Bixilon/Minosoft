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

package de.bixilon.minosoft.game.datatypes.player;

public enum PingBars {
    NO_CONNECTION,
    BARS_5,
    BARS_4,
    BARS_3,
    BARS_2,
    BRAS_1;

    public static PingBars byPing(long ping) {
        if (ping < 0) {
            return NO_CONNECTION;
        } else if (ping < 150) {
            return BARS_5;
        } else if (ping < 300) {
            return BARS_4;
        } else if (ping < 600) {
            return BARS_3;
        } else if (ping < 1000) {
            return BARS_2;
        } else {
            return BRAS_1;
        }
    }
}