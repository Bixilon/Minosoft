/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.parser.minecraft.time

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

enum class TimeUnit(val multiplier: Int) {
    TICKS(1),
    SECONDS(ProtocolDefinition.TICKS_PER_SECOND),
    DAYS(ProtocolDefinition.TICKS_PER_DAY),
    ;

    companion object {
        val UNITS = listOf("t", "s", "d")

        fun fromUnit(char: Int?): TimeUnit {
            return when (char) {
                null, 't'.code -> TICKS
                's'.code -> SECONDS
                'd'.code -> DAYS
                else -> throw IllegalArgumentException("Invalid unit: ${char.toChar()}")
            }
        }
    }
}
