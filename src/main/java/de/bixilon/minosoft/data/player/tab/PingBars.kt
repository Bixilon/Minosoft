/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.player.tab

enum class PingBars {
    NO_CONNECTION,
    BARS_5,
    BARS_4,
    BARS_3,
    BARS_2,
    BARS_1,
    ;

    companion object {

        @JvmStatic
        fun byPing(ping: Long): PingBars {
            return when {
                ping < 0 -> NO_CONNECTION
                ping < 150 -> BARS_5
                ping < 300 -> BARS_4
                ping < 600 -> BARS_3
                ping < 1000 -> BARS_2
                else -> BARS_1
            }
        }
    }
}
