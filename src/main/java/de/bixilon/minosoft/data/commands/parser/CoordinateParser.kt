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
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.MixWorldAndLocalCoordinatesCommandParseException

abstract class CoordinateParser : CommandParser() {

    private fun readCoordinate(stringReader: CommandStringReader, allowDecimal: Boolean): CoordinateNotations {
        val notation: CoordinateNotations = when (stringReader.peek()) {
            '~' -> {
                CoordinateNotations.RELATIVE
            }
            '^' -> {
                CoordinateNotations.LOCALE
            }
            else -> {
                CoordinateNotations.NONE
            }
        }
        if (notation != CoordinateNotations.NONE) {
            stringReader.skip()
        }
        if (!stringReader.canRead() || stringReader.peek() == ' ') {
            return notation
        }
        if (allowDecimal) {
            stringReader.readDouble()
        } else {
            stringReader.readInt()
        }
        return notation
    }

    fun readCoordinates(stringReader: CommandStringReader, allowDecimal: Boolean, count: Int) {
        var localCoordinates: Boolean? = null
        // read coordinates and check if somebody tries to mix world (~ or number) and local coordinates (^)
        for (i in 0 until count) {
            val notation = readCoordinate(stringReader, allowDecimal)
            stringReader.skipWhitespaces()
            if (localCoordinates == null) {
                localCoordinates = notation == CoordinateNotations.LOCALE
            } else {
                if ((notation == CoordinateNotations.LOCALE && !localCoordinates) || (notation != CoordinateNotations.LOCALE && localCoordinates)) {
                    throw MixWorldAndLocalCoordinatesCommandParseException(stringReader, "") // ToDo: print argument
                }
            }

        }
    }

    enum class CoordinateNotations {
        NONE,
        RELATIVE,
        LOCALE
    }
}
